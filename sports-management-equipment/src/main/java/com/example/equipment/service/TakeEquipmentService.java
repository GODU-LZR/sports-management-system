package com.example.equipment.service;

import com.example.common.constant.UserConstant;
import com.example.equipment.dto.LocaleBorrowDTO;
import com.example.equipment.dto.UserOperateEquipmentDTO;
import io.swagger.v3.oas.annotations.Parameter;

public interface TakeEquipmentService {
    void UpdateOrderEquipment(UserOperateEquipmentDTO takeEquipmentDTO, UserConstant currentUser,boolean able);

    void OutboundReserveEquipment(Long equipmentId,UserConstant currentUser);

    void returnEquipment(Long equipmentId,UserConstant currentUser);

    void LocaleOutboundEquipment(LocaleBorrowDTO borrowDTO, UserConstant currentUser);
}
