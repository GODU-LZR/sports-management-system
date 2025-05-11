package com.example.equipment.service;

import com.example.common.constant.UserConstant;
import com.example.equipment.dto.UserOperateEquipmentDTO;

public interface TakeEquipmentService {
    void UpdateOrderEquipment(UserOperateEquipmentDTO takeEquipmentDTO, UserConstant currentUser,boolean able);
}
