package com.example.equipment.service.impl;

import com.example.equipment.dto.EquipmentDTO;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.service.EquipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.lang.Math.random;


@Service
@Slf4j
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;


    /**
     * 新增器材方法
     * @param equipmentDTO
     */
    @Override
    public void addEquipment(EquipmentDTO equipmentDTO) {
        //设置当前添加的用户id
        Long id = 1L;   //当前先默认为1
        equipmentDTO.setCreateId(id);
        Long Eid =100L;
        equipmentDTO.setEquipmentId(Eid);
        equipmentDTO.setCreateTime(LocalDateTime.now());  //当前时间

        log.info("Service层添加器材的信息:{}",equipmentDTO);
        equipmentMapper.addEquipment(equipmentDTO);
    }

    /**
     * 修改器材信息
     * @param equipmentDTO
     */
    @Override
    public void updateEquipment(EquipmentDTO equipmentDTO) {


        if(equipmentDTO !=null){
            equipmentMapper.updateEquipment(equipmentDTO);
        }

    }


    /**
     * 根据Id删除器材
     * @param equipmentId
     */
    @Override
    public void deleteEquipment(Long equipmentId) {

        if (equipmentId !=null){
            equipmentMapper.delete(equipmentId);
        }

    }
}
