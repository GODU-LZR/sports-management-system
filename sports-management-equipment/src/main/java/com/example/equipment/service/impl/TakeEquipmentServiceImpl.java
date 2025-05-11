package com.example.equipment.service.impl;

import com.example.common.constant.UserConstant;
import com.example.equipment.dto.UserOperateEquipmentDTO;
import com.example.equipment.mapper.CategoryMapper;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.mapper.EquipmentOrderItemMapper;
import com.example.equipment.mapper.EquipmentOrderMapper;
import com.example.equipment.pojo.OrderItem;
import com.example.equipment.service.TakeEquipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TakeEquipmentServiceImpl implements TakeEquipmentService {

    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    EquipmentMapper equipmentMapper;
    @Autowired
    EquipmentOrderItemMapper ItemMapper;
    @Autowired
    EquipmentOrderMapper equipmentOrderMapper;

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
}
