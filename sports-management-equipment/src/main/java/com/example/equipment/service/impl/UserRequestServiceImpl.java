package com.example.equipment.service.impl;

import com.example.equipment.constant.OrderEquipmentStatusConstant;
import com.example.equipment.pojo.OrderItem;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.constant.UserConstant;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.RevokeRequestDTO;
import com.example.equipment.dto.SelectUserRequestDTO;
import com.example.equipment.mapper.*;
import com.example.equipment.pojo.*;
import com.example.equipment.service.UserRequestService;
import com.example.equipment.vo.AdminRequestVO;
import com.example.equipment.vo.RequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserRequestServiceImpl extends ServiceImpl<RequestMapper, RequestVO> implements UserRequestService {

    @Autowired
    RequestMapper requestMapper;

    @Autowired
    EquipmentMapper equipmentMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    EquipmentOrderMapper orderMapper;

    @Autowired
    EquipmentOrderItemMapper orderItemMapper;

    @Autowired
    SnowflakeIdGenerator snowflakeIdGenerator;

    private static final int CANCEL_THRESHOLD_MINUTES = 10;

    // 定义自动审核的数量阈值
    private static final int AUTO_APPROVE_QUANTITY_THRESHOLD = 5; // 例如：请求数量小于等于5个时自动审核


    /**
     * 处理器材借用请求
     *
     * @param borrowRequestDTO 借用请求数据
     * @param currentUser      当前用户
     */
    @Override
    @Transactional // 确保整个方法内的数据库操作是原子性的
    public void borrowEquipment(BorrowRequestDTO borrowRequestDTO, UserConstant currentUser) {

        // 1. 基本验证和初始化
        LocalDateTime now = LocalDateTime.now();
        Long userId = currentUser.getUserId();
        long snowId = snowflakeIdGenerator.nextId(); // 雪花生成请求Id

        // 检查开始时间和结束时间是否为空
        if (borrowRequestDTO.getStartTime() == null || borrowRequestDTO.getEndTime() == null) {
            throw new IllegalArgumentException("预约时间和结束时间不能为空");
        }

        // 检查开始时间是否在当前时间之后
        if (borrowRequestDTO.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("预约时间必须在当前时间之后");
        }

        // 检查结束时间是否在开始时间之后
        if (borrowRequestDTO.getEndTime().isBefore(borrowRequestDTO.getStartTime())) {
            throw new IllegalArgumentException("预约结束时间必须在开始时间之后");
        }

        // 计算借用时长 (分钟)
        Duration duration = Duration.between(borrowRequestDTO.getStartTime(), borrowRequestDTO.getEndTime());
        long durationMinutes = duration.toMinutes(); // 获取分钟数

        // 检查时长是否大于等于30分钟
        if (durationMinutes < 30) {
            throw new IllegalArgumentException("预约时间在30分钟起步");
        }

        // **计算按半小时计费的有效时长 (小时)**
        // 计算半小时块数，不足半小时按半小时计算 (向上取整)
        long numHalfHourBlocks = (durationMinutes + 29) / 30;
        // 计算有效的计费小时数
        double effectiveDurationHours = numHalfHourBlocks * 0.5;
        log.info("原始借用时长: {} 分钟, 按半小时计费有效时长: {} 小时 ({} 个半小时块)",
                durationMinutes, effectiveDurationHours, numHalfHourBlocks);


        // 获取前端传送的器材列表
        List<BorrowEquipment> borrowEquipmentList = borrowRequestDTO.getEquipmentList();

        // 检查器材列表是否为空
        if (borrowEquipmentList == null || borrowEquipmentList.isEmpty()) {
            throw new IllegalArgumentException("借用器材列表不能为空");
        }

        // **2. 确定整个请求是否需要手动审核**
        // 遍历所有请求的器材，检查是否存在数量超过阈值的项
        boolean overallNeedsManualReview = false;
        for (BorrowEquipment item : borrowEquipmentList) {
            // 检查数量是否大于0
            Integer requestedQuantity = item.getQuantity();
            if (requestedQuantity == null || requestedQuantity <= 0) {
                throw new IllegalArgumentException("器材 '" + item.getName() + "' 的预约数量必须大于0");
            }

            // 如果任一器材数量超过自动审核阈值，则整个请求需要手动审核
            if (requestedQuantity > AUTO_APPROVE_QUANTITY_THRESHOLD) {
                overallNeedsManualReview = true;
                // 找到一个超阈值的就可以确定需要手动审核了，可以提前跳出循环
                break;
            }
        }

        // **3. 根据是否需要手动审核确定整个请求的最终状态**
        // 0: 审核中 (需要手动审核)
        // 1: 自动通过 (不需要手动审核，且所有器材数量都在阈值内)
        int finalRequestStatus = overallNeedsManualReview ? 0 : 1;
        log.info("请求ID {} 确定最终状态: {}", snowId, finalRequestStatus == 1 ? "自动通过" : "审核中");

        // 收集所有自动批准生成 OrderItem 的列表 (仅在自动通过时使用)
        List<OrderItem> orderItemsToInsert = new ArrayList<>();
        // 用于累加总金额 (仅在自动通过时使用)
        double totalAmount = 0.0;

        // **4. 遍历器材列表，执行可用性检查和处理**
        // 如果在处理某个器材时发现可用性不足，整个请求应该失败（通过抛异常和事务回滚）
        for (BorrowEquipment item : borrowEquipmentList) {
            Integer requestedQuantity = item.getQuantity();

            // 4.1. 根据器材类型名称查询该器材分类
            EquipmentCategory equipmentCategory = categoryMapper.selectByName(item.getName());

            log.info("用户请求的器材分类为:{}", equipmentCategory);
            // 如果器材分类不存在
            if (equipmentCategory == null) {
                throw new IllegalArgumentException("器材分类 '" + item.getName() + "' 不存在");
            }

            // 获取该分类的每小时价格
            float hourlyPrice = equipmentCategory.getValue();
            if (hourlyPrice < 0) { // 简单的价格校验
                throw new IllegalArgumentException("器材分类 '" + item.getName() + "' 的价格无效");
            }

            // **计算单个器材在整个借用时长内的费用 (使用有效时长)**
            double itemCost = hourlyPrice * effectiveDurationHours;

            // 4.2. 查询在指定时间段内可用的具体器材ID列表
            // Mapper方法使用了 limit，只会返回最多 requestedQuantity 个可用器材ID
            List<Long> availableEquipmentIds = equipmentMapper.findAvailableEquipmentIds(
                    equipmentCategory.getCategoryId(),
                    borrowRequestDTO.getStartTime(),
                    borrowRequestDTO.getEndTime(),
                    requestedQuantity // 将请求数量作为 limit 参数传入
            );
            log.info("为器材分类 '{}' (ID: {}) 查询到在 {} 到 {} 时间段内可用的器材ID列表 (最多{}个): {}",
                    item.getName(), equipmentCategory.getCategoryId(), borrowRequestDTO.getStartTime(),
                    borrowRequestDTO.getEndTime(), requestedQuantity, availableEquipmentIds);

            // **4.3. 检查实际找到的可用器材数量是否满足请求数量**
            // 这是非常重要的检查，确保在指定时间段内有足够的具体器材可用
            if (availableEquipmentIds.size() < requestedQuantity) {
                // 如果实际可用数量不足，抛出异常，整个请求失败并回滚
                throw new IllegalArgumentException("器材 '" + item.getName() + "' 在指定时间段内可用数量不足 ("
                        + availableEquipmentIds.size() + "/" + requestedQuantity + ")，请重新预约");
            }

            // **4.4. 遍历找到的可用器材ID，进行处理**
            for (Long equipmentId : availableEquipmentIds) { // 直接遍历找到的可用器材ID

                // **4.5. 根据最终状态处理：插入请求记录 或 准备订单明细**
                if (finalRequestStatus == 0) { // 需要手动审核
                    EquipmentBorrowRequest request = new EquipmentBorrowRequest();
                    // 拷贝基本属性
                    BeanUtils.copyProperties(borrowRequestDTO, request);
                    request.setEquipmentId(equipmentId); // 设置具体的器材ID
                    request.setRequestId(snowId);       // 使用同一个请求ID
                    request.setCreateTime(now);         // 创建时间
                    request.setUserId(userId);          // 用户ID
                    request.setQuantity(1);             // 每条记录代表一个具体的器材，数量为1
                    request.setStatus(0); // 审核中
                    request.setIsRevoked(0);            // 默认未撤销

                    log.info("手动审核流程：准备为器材ID {} 插入请求记录 (请求ID: {}, 状态: {})", equipmentId, snowId, 0);
                    requestMapper.insertBorrowRequest(request);

                } else { // finalRequestStatus == 1, 自动通过

                    EquipmentBorrowRequest request = new EquipmentBorrowRequest();
                    // 拷贝基本属性
                    BeanUtils.copyProperties(borrowRequestDTO, request);
                    request.setEquipmentId(equipmentId); // 设置具体的器材ID
                    request.setRequestId(snowId);       // 使用同一个请求ID
                    request.setCreateTime(now);         // 创建时间
                    request.setUserId(userId);          // 用户ID
                    request.setQuantity(1);             // 每条记录代表一个具体的器材，数量为1
                    request.setStatus(1); // 审核中
                    request.setIsRevoked(0);            // 默认未撤销

                    log.info("自动审核流程：准备为器材ID {} 插入请求记录 (请求ID: {}, 状态: {})", equipmentId, snowId, 1);
                    requestMapper.insertBorrowRequest(request);


                    // 为这些自动通过的具体器材创建 OrderItem 并计算费用
                    OrderItem orderItem = new OrderItem();
                    // orderId 会在订单主表插入后设置
                    orderItem.setEquipmentId(equipmentId);
//                    orderItem.setQuantity(1); // 具体器材数量为1
//                    orderItem.setUnitPrice((double) hourlyPrice); // 存储每小时单价 (或者根据业务需要存储计算后的总价/数量)
                    orderItem.setItemAmount(BigDecimal.valueOf(itemCost)); // 存储单个器材的总费用
                    // 设置明细项初始状态为“待借出” (使用 OrderEquipmentStatusConstant)
                    orderItem.setItemStatusId(OrderEquipmentStatusConstant.PENDING_BORROW.getId()); // 假设 PENDING_BORROW 的ID是1
                    // 可以选择性地关联到原始请求ID，如果 EquipmentBorrowRequest 记录也被创建了
                    // orderItem.setRequestId(snowId);

                    orderItemsToInsert.add(orderItem);

                    log.info("自动通过流程：为器材ID {} 准备订单明细项，费用: {}", equipmentId, itemCost);
                    // 在自动通过流程中，我们不再向 EquipmentBorrowRequest 表插入状态为1的记录
                    // 而是直接创建订单和订单明细项。
                }

                // **4.6. 更新器材分类的账面库存**
                // 无论请求状态如何，这些具体的器材都已经被本次请求“预定”或“申请”，账面库存应该减少
                // 假设 categoryMapper.reduceBookStock(equipmentId) 会找到 equipmentId 对应的分类并将其账面库存减1
                categoryMapper.reduceBookStock(equipmentId);
                log.info("器材ID {} 对应的分类账面库存已更新", equipmentId);
            }
            // 在处理完一个器材类型的可用器材后，将该类型所有器材的总费用累加到总金额
            if (finalRequestStatus == 1) {
                totalAmount += itemCost * availableEquipmentIds.size(); // itemCost 是单个器材的费用
            }
        }

        // **5. 如果是自动通过，创建订单和订单明细项**
        // 这个逻辑必须在处理完所有器材项的可用性检查和库存更新之后执行
        if (finalRequestStatus == 1) {
            long OrderSnowId = snowflakeIdGenerator.nextId();
            log.info("请求ID {} 自动通过，开始创建订单...", OrderSnowId);

            // 5.1. 创建订单主表实体
            Order order = new Order();
            order.setUserId(userId);
            order.setRequestId(OrderSnowId); // 关联到原始请求ID
            order.setRequestStartTime(borrowRequestDTO.getStartTime());
            order.setRequestEndTime(borrowRequestDTO.getEndTime());
            order.setCreateTime(now);
            order.setTotalAmount(BigDecimal.valueOf(totalAmount)); // 使用累加的总金额
            // 订单状态设置为“待支付”或“已借出”，取决于您的业务流程
            // 如果借用需要先支付，状态为待支付
            order.setOrderStatusId(OrderStatusConstant.PENDING_PAYMENT.getId()); // 假设 PENDING_PAYMENT 的ID是X
            // 如果借用是免费的或后付费，可以直接设置为“已借出”
            // order.setStatus(OrderStatusConstant.BORROWED.getId()); // 假设 BORROWED 的ID是Y

            // 5.2. 插入订单主表并获取生成的订单ID
            orderMapper.insert(order);
            Long orderId = order.getOrderId(); // 假设 insertOrder 会回填生成的ID

            log.info("请求ID {} 自动通过，成功创建订单，订单ID: {}", snowId, orderId);

            // 5.3. 插入订单明细项
            for (OrderItem orderItem : orderItemsToInsert) {
                orderItem.setOrderId(orderId); // 设置关联的订单ID
                orderItemMapper.insert(orderItem);
                log.info("为订单ID {} 插入明细项，器材ID: {}", orderId, orderItem.getEquipmentId());
            }

            // 5.4. (可选) 更新原始请求记录状态
            // 如果您选择在自动通过时也插入 EquipmentBorrowRequest 记录 (状态1)，
            // 可以在这里更新其状态到“已转订单”并关联 orderId。
            // 如果自动通过不插入 EquipmentBorrowRequest 记录，则忽略此步。
        }

        // 整个请求处理完成，如果走到这里说明所有器材都通过了可用性检查并插入了请求记录/创建了订单
        log.info("请求ID {} 处理完成，最终状态: {}", snowId, finalRequestStatus == 1 ? "自动通过并生成订单" : "审核中");
    }


    /**
     * 用户撤销未审核
     * 已通过 的申请
     * @param requestDTO
     * @param currentUser
     * @return
     */
    @Override
    public String revoke(RevokeRequestDTO requestDTO, UserConstant currentUser) {

        Long requestId = requestDTO.getRequestId();

        // 根据用户需要撤销的请求Id进行查询
        List<RequestVO> list = requestMapper.selectUnrevoke(requestId);

        log.info("当前用户根据请求Id查询的未撤销请求数据为:{}", list);

        if (list == null || list.isEmpty()) {
            return "未找到相关预约记录";
        }

        RequestVO request = list.get(0);           //因为同一个请求下的审核状态都是一致的
        LocalDateTime now = LocalDateTime.now();  //因为同一个请求下的预约时间都是一致的

        // 如果当前时间在预约开始时间之前
        if (now.isBefore(request.getStartTime())) {
            System.out.println("当前时间在预约时间之前");

            // 计算时间差
            Duration duration = Duration.between(now, request.getStartTime());

            // 如果差值小于10分钟，用户不能进行撤销
            if (duration.toMinutes() < CANCEL_THRESHOLD_MINUTES) {
                throw new IllegalArgumentException("距离预约时间已不足十分钟，无法撤销");
            }

            //如果是未审核的请求
            if (request.getStatus() == 0) {
                // 执行撤销逻辑
                for (RequestVO item : list) {
                    //用户可以无偿撤销租借请求
                    requestMapper.setRevoke(item.getRequestId());

                    //对器材分类表的账面库存增加
                    categoryMapper.raiseBookStock(item.getEquipmentId());
                }
                return "已撤销!";
            } else {

                //对已审核通过 且未到预约时间的请求 的逻辑处理
                // 执行撤销逻辑
                for (RequestVO item : list) {
                    //用户可以无偿撤销租借请求
                    requestMapper.setRevoke(item.getRequestId());

                    equipmentMapper.setEquipment_Status_To_1(item.getEquipmentId());
                    //对器材分类表的账面库存增加
                    categoryMapper.raiseBookStock(item.getEquipmentId());

//                    categoryMapper.ReturnEqp(item.getEquipmentId());
                }
                //前端中已提醒 用户 对已审核通过的请求 进行撤销的话，需要支付一定的违约金
                return "撤销成功";
            }
        } else {
            // 当前时间已超过预约时间
            return "预约时间已过，是否仍要撤销？但需要支付一定的违约金";
        }
    }

//    /**
//     * 分页查询指定用户提交的器材申请
//     * @param query  分页及查询参数 DTO
//     * @param userId 当前用户的ID
//     * @return 分页结果 IPage<RequestVO>
//     */
//    @Override // 如果是实现接口方法，加上 @Override
//    public IPage<RequestVO> getUserRequestsPage(RequestPageQuery query, Long userId) {
//
//        // 1. 创建分页对象
//        IPage<RequestVO> page = new Page<>(query.getPageNum(), query.getPageSize());
//
//        // 2. 创建查询 Wrapper
//        LambdaQueryWrapper<RequestVO> queryWrapper = new LambdaQueryWrapper<>();
//
//        // 3. 添加查询条件：用户ID等于当前用户ID
//        queryWrapper.eq(RequestVO::getUserId, userId);
//
//        // >>> 在这里添加根据状态查询的条件 <<<
//        // 假设 RequestPageQuery 对象有一个 getStatus() 方法来获取用户选择的状态
//        Integer status = query.getStatus();
//        if (status != null) {
//            // 如果状态值不为空，则添加状态等于该值的条件
//
//            // 假设 RequestVO 中表示状态的字段对应的方法是 getStatus()
//            queryWrapper.eq(RequestVO::getStatus, status);
//        }
//        // --------------------------------------
//
//        // 4. (可选) 添加排序，例如按申请时间倒序
//        queryWrapper.orderByDesc(RequestVO::getStartTime);
//
//        // 打印最终的 Wrapper 内容（可选，用于调试）
//        // log.info("Final queryWrapper: {}", queryWrapper.getExpression()); // getExpression() 可以打印一部分信息
//
//        // 5. 执行分页查询
//        IPage<RequestVO> resultPage = requestMapper.selectPage(page, queryWrapper);
//
//        return resultPage;
//    }

    @Override
    public List<AdminRequestVO> getUserRequest(SelectUserRequestDTO requestDTO, UserConstant currentUser) {

        if (requestDTO == null || currentUser ==null){
            throw new IllegalArgumentException("请求参数或未登录");
        }

        Long userId = currentUser.getUserId();

        List<AdminRequestVO> list = requestMapper.selectUserRequestsByCriteria(requestDTO,userId);

        return list;
    }


}
