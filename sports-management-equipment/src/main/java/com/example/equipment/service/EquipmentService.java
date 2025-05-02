package com.example.equipment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.equipment.dto.EquipmentDTO;
import com.example.equipment.dto.utilDTO.EquipmentPageQuery;
import com.example.equipment.vo.EquipmentVO;

public interface EquipmentService {
    void addEquipment(EquipmentDTO equipmentDTO, UserConstant userConstant);

    void updateEquipment(EquipmentDTO equipmentDTO,UserConstant userConstant);

    void deleteEquipment(Long equipmentId,UserConstant userConstant);

    IPage<EquipmentVO> PageSelect(EquipmentPageQuery query);
}
