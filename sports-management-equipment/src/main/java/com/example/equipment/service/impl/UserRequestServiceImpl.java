package com.example.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.constant.UserConstant;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.RevokeRequestDTO;
import com.example.equipment.dto.SelectAllRequestDTO;
import com.example.equipment.dto.SelectUserRequestDTO;
import com.example.equipment.dto.utilDTO.RequestPageQuery;
import com.example.equipment.mapper.CategoryMapper;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.mapper.RequestMapper;
import com.example.equipment.pojo.*;
import com.example.equipment.service.UserRequestService;
import com.example.equipment.vo.AdminRequestVO;
import com.example.equipment.vo.RequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
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

        // 计算借用时长
        Duration duration = Duration.between(borrowRequestDTO.getStartTime(), borrowRequestDTO.getEndTime());

        // 检查时长是否大于等于30分钟
        if (duration.toMinutes() < 30) {
            throw new IllegalArgumentException("预约时间在30分钟起步");
        }

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


        // **4. 遍历器材列表，执行可用性检查和插入请求记录**
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

//           if(item.getQuantity()> equipmentCategory.getBookStock()) {
//               throw new IllegalArgumentException("账面库存不足");
//           }


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

            // **4.4. 插入请求记录并更新相关状态**
            // 遍历找到的可用器材ID，为每个器材创建一个请求记录
            // 因为上面的检查已经确保了 availableEquipmentIds.size() == requestedQuantity (如果找到足够的话)
            for (Long equipmentId : availableEquipmentIds) { // 直接遍历找到的可用器材ID
                EquipmentBorrowRequest request = new EquipmentBorrowRequest();

                // 拷贝基本属性
                BeanUtils.copyProperties(borrowRequestDTO, request);

                request.setEquipmentId(equipmentId); // 设置具体的器材ID
                request.setRequestId(snowId);       // 使用同一个请求ID
                request.setCreateTime(now);         // 创建时间
                request.setUserId(userId);          // 用户ID
                request.setQuantity(1);             // 每条记录代表一个具体的器材，数量为1
                request.setStatus(finalRequestStatus); // **使用整个请求的最终状态**
                request.setIsRevoked(0);            // 默认未撤销

                log.info("准备为器材ID {} 插入请求记录 (请求ID: {}, 状态: {})", equipmentId, snowId, finalRequestStatus);
                requestMapper.insertBorrowRequest(request);

                 // **4.5. 根据最终状态更新具体器材的状态**

                //如果是自动审核 的 自动生成一个订单
                if (finalRequestStatus == 1) {
//                    // 假设 equipmentMapper.setEquipment_Status_To_0(equipmentId) 将器材状态更新为已借出/使用中 (例如状态码为0)
//                    equipmentMapper.setEquipment_Status_To_0(equipmentId);
//                    log.info("器材ID {} 状态更新为 0 (已借出),请求已自动通过", equipmentId);

                }

                // **4.6. 更新器材分类的账面库存**
                // 无论请求状态如何，这些具体的器材都已经被本次请求“预定”或“申请”，账面库存应该减少
                // 假设 categoryMapper.setBookStock(equipmentId) 会找到 equipmentId 对应的分类并将其账面库存减1
                categoryMapper.reduceBookStock(equipmentId);
                log.info("器材ID {} 对应的分类账面库存已更新", equipmentId);
            }
        }

        // 整个请求处理完成，如果走到这里说明所有器材都通过了可用性检查并插入了请求记录
        log.info("请求ID {} 处理完成，最终状态: {}", snowId, finalRequestStatus == 1 ? "自动通过" : "审核中");
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
