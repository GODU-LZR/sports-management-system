package com.example.equipment.service;

import com.example.equipment.dto.EquipmentDTO;

public interface EquipmentService {
    void addEquipment(EquipmentDTO equipmentDTO);

    void updateEquipment(EquipmentDTO equipmentDTO);

    void deleteEquipment(Long equipmentId);
}
