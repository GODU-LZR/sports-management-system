package com.example.equipment.service;

import com.example.common.constant.UserConstant;
import com.example.equipment.dto.EquipmentDTO;

public interface EquipmentService {
    void addEquipment(EquipmentDTO equipmentDTO, UserConstant userConstant);

    void updateEquipment(EquipmentDTO equipmentDTO,UserConstant userConstant);

    void deleteEquipment(Long equipmentId,UserConstant userConstant);
}
