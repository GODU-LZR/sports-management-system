package com.example.equipment.service.impl;

import com.example.common.constant.UserConstant;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.equipment.dto.BorrowRequestDTO;
import com.example.equipment.dto.RevokeRequestDTO;
import com.example.equipment.mapper.CategoryMapper;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.mapper.RequestMapper;
import com.example.equipment.pojo.EquipmentBorrowRequest;
import com.example.equipment.pojo.EquipmentId;
import com.example.equipment.service.UserRequestService;
import com.example.equipment.vo.RequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserRequestServiceImpl implements UserRequestService {

    @Autowired
    RequestMapper requestMapper;

    @Autowired
    EquipmentMapper equipmentMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    SnowflakeIdGenerator snowflakeIdGenerator;

    //    是一个常量（constant），它定义了用户能够取消预约的时间阈值
    private static final int CANCEL_THRESHOLD_MINUTES = 10;

    @Override
    public void borrowEquipment(BorrowRequestDTO borrowRequestDTO, UserConstant currentUser) {

        List<EquipmentId> list = borrowRequestDTO.getEquipmentIds();   //获取DTO里
        log.info("租借请求中的器材列表为:{}", list);

        LocalDateTime time = LocalDateTime.now();   //设置创建时间
        Long userId = currentUser.getUserId();     //当前用户的Id
        long snowId = snowflakeIdGenerator.nextId();   //雪花生成请求Id
        for (EquipmentId item : list) {

            Integer status = equipmentMapper.ReturnStatus(item.getEquipmentId());

            if (status == 0) {
                //            对于租借申请里 的 每个器材Id 循环地插入 到请求表里
                EquipmentBorrowRequest request = new EquipmentBorrowRequest();  //每次new一个 器材请求的对象

                BeanUtils.copyProperties(borrowRequestDTO, request);  //拷贝属性

                request.setEquipmentId(item.getEquipmentId());
                request.setRequestId(snowId);
                request.setCreateTime(time);
                request.setUserId(userId);

                log.info("Service层的器材请求对象为:{}", request);
                requestMapper.insertBorrowRequest(request);
            }
        }


    }


    /**
     * 用户撤销未审核
     * 已通过 的申请
     *
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

        RequestVO request = list.get(0);
        LocalDateTime now = LocalDateTime.now();

        // 如果当前时间在预约开始时间之前
        if (now.isBefore(request.getStartTime())) {
            System.out.println("当前时间在预约时间之前");

            // 计算时间差
            Duration duration = Duration.between(now, request.getStartTime());

            // 如果差值小于10分钟，用户不能进行撤销
            if (duration.toMinutes() < CANCEL_THRESHOLD_MINUTES) {
                return "距离预约时间已不足十分钟，无法撤销";
            }

            //如果是未审核的请求
            if (request.getStatus() ==0){
                // 执行撤销逻辑
                for (RequestVO item : list) {
                    //用户可以无偿撤销租借请求
                   requestMapper.setRevoke(item.getRequestId());
                }
                return "已撤销!";
            }
            else{

                //对已审核通过 且未到预约时间的请求 的逻辑处理

                return "撤销成功";
            }


        } else {
            // 当前时间已超过预约时间
            return "预约时间已过，无法撤销";
        }
    }

}
