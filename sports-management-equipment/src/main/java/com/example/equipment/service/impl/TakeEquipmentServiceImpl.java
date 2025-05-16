package com.example.equipment.service.impl;

import com.example.common.constant.UserConstant;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.equipment.constant.OrderEquipmentStatusConstant;
import com.example.equipment.dto.LocaleBorrowDTO;
import com.example.equipment.dto.UserOperateEquipmentDTO;
import com.example.equipment.mapper.*;
import com.example.equipment.pojo.EquipmentBorrowRequest;
import com.example.equipment.pojo.Order;
import com.example.equipment.pojo.OrderItem;
import com.example.equipment.service.TakeEquipmentService;
import com.example.equipment.vo.RequestVO;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TakeEquipmentServiceImpl implements TakeEquipmentService {


    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    EquipmentMapper equipmentMapper;


    @Autowired
    RequestMapper requestMapper;
    @Autowired
    EquipmentOrderItemMapper ItemMapper;
    @Autowired
    EquipmentOrderMapper equipmentOrderMapper;


    @Autowired
    SnowflakeIdGenerator snowflakeIdGenerator;
    @Override
    @Transactional
    public void UpdateOrderEquipment(UserOperateEquipmentDTO equipmentDTO, UserConstant currentUser, boolean available) {

        Long equipmentId = equipmentDTO.getEquipmentId();
        String operation = equipmentDTO.getOperation(); // "领取" 或 "归还"
        Long scanningUserId = currentUser.getUserId(); // 扫码操作的用户ID
        LocalDateTime now = LocalDateTime.now();

        //定义一个布尔值 用于接收是否是可用的状态
        //再结合有无扫码等操作给对应的  equipment_borrow_detail 中的状态修改

        OrderItem orderItem = ItemMapper.selectByEquipmentId(equipmentId);

        log.info("查询到未使用和未领取的器材列表为:{}", orderItem);

        if (available) {
            if (equipmentId != null && operation.equals("领取")) {

                //DTO中有器材Id  且操作为领取

                //那么就将器材订单的状态设置为 已借出
//               ItemMapper.updateOrderStatus(OrderEquipmentStatusConstant.BORROWED.getId(),orderItem.getDetailId());
//
//               //将状态设置为0  不可用
//               equipmentMapper.setEquipment_Status_To_0(takeEquipmentDTO.getEquipmentId());

//               //出库接口
//               categoryMapper.BorrowEqp(takeEquipmentDTO.getEquipmentId());

            } else if (equipmentId!=null && operation.equals("归还")) {
                //进行归还操作
//                将器材订单表里面的状态设置为 等待领取
//                更新器材状态可用
//                更新物理库存  book_stock
            }   //这两个可以合并在一起
            else {
            // 可用的状态下  没有扫码的操作  即器材Id为空  operation字段为空
            //更新器材状态为  等待领取
            }

        } else {
            //不可用的时候
            if (equipmentId != null && operation.equals("领取")) {
//                不可用 且 扫码 领取
//                定义为非法领取
//                ItemMapper.updateById(OrderEquipmentStatusConstant.ILLEGAL_PICKUP);
            } else if (equipmentId!=null && operation.equals("归还")) {
                //该情况下为  归还  更新器材订单状态
            }
            else {
                //不可用状态下 没有扫码  没有operation
                //更新器材订单为  未使用
            }
        }
    }



    /**
     * 预约器材出库
     *
     * 对器材分类表的物理库存进行变动
     *
     * 对器材表进行设置器材状态
     *
     * 对器材订单 equipment_borrow_detail 表的对应状态修改
     * @param equipmentId
     */
    @Override
    public void OutboundReserveEquipment(Long equipmentId,UserConstant currentUser) {

        if(equipmentId==null)
        {
            throw new IllegalArgumentException("器材为空");
        }
        Long userId = currentUser.getUserId();   //拿到用户Id

        LocalDateTime now = LocalDateTime.now();

       List<RequestVO> list =  requestMapper.findPassedRequestByEquipmentId(equipmentId,userId,now);

       log.info("通过器材Id查询到当前用户预约的列表为:{},大小为:{}",list,list.size());

       for (RequestVO item : list)
       {

           //出库
           categoryMapper.BorrowEqp(item.getEquipmentId());

           equipmentMapper.setEquipment_Status_To_0(item.getEquipmentId());

           requestMapper.updateStatus_TO_5(item.getRequestId(),item.getEquipmentId());

       }

    }

    //归还器材
    @Override
    public void returnEquipment(Long equipmentId,UserConstant currentUser) {

        Long userId = currentUser.getUserId();   //拿到用户Id

        LocalDateTime now = LocalDateTime.now();

        List<RequestVO> list =  requestMapper.findBorrowed_RequestByEquipmentId(equipmentId,userId);

        log.info("当前用户预约后已领取的列表为:{},大小为:{}",list,list.size());

        for (RequestVO item : list)
        {

            //可用
            equipmentMapper.setEquipment_Status_To_1(equipmentId);


            //入库
            categoryMapper.ReturnEqp(equipmentId);

            //状态设置为归还
            requestMapper.updateStatus_TO_3(item.getRequestId(),item.getEquipmentId());

        }

    }


    //现场拿器材
    @Override
    public void LocaleOutboundEquipment(LocaleBorrowDTO borrowDTO, UserConstant currentUser) {

        Long equipmentId = borrowDTO.getEquipmentId();

        LocalDateTime startTime = borrowDTO.getStartTime();

        LocalDateTime endTime = borrowDTO.getEndTime();

        Long userId = currentUser.getUserId();

        Integer status = equipmentMapper.ReturnStatus(equipmentId);

        log.info("当前租借器材的状态为:{}",status);
        //可用
        if(status ==1)
        {
            EquipmentBorrowRequest request = new EquipmentBorrowRequest();

            // 拷贝基本属性
            BeanUtils.copyProperties(borrowDTO, request);

            request.setEquipmentId(equipmentId); // 设置具体的器材ID
            request.setRequestId(snowflakeIdGenerator.nextId());       // 使用同一个请求ID
            request.setCreateTime(borrowDTO.getStartTime());         // 创建时间
            request.setUserId(userId);          // 用户ID
            request.setQuantity(1);             // 每条记录代表一个具体的器材，数量为1
            request.setStatus(5); // 现场拿
            request.setIsRevoked(0);            // 默认未撤销

            requestMapper.insertBorrowRequest(request);

//            器材状态不可用
            equipmentMapper.setEquipment_Status_To_0(equipmentId);

            categoryMapper.BorrowEqp(equipmentId);  //物理库存减一

            categoryMapper.reduceBookStock(equipmentId);//账面库存减一
        }
        else{
            throw new  IllegalArgumentException("当前器材不可用");
        }
    }
}
