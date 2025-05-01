package com.example.equipment.service.impl;

import com.example.common.constant.UserConstant;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.equipment.dto.EquipmentDTO;
import com.example.equipment.mapper.CategoryMapper;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.pojo.Equipment;
import com.example.equipment.service.EquipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.lang.Math.random;


@Service
@Slf4j
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 新增器材方法
     * @param equipmentDTO
     */
    @Override
    public void addEquipment(EquipmentDTO equipmentDTO, UserConstant user) {

        Equipment equipment = new Equipment();

        //拷贝属性
        BeanUtils.copyProperties(equipmentDTO,equipment);

        equipment.setEquipmentId(snowflakeIdGenerator.nextId());  //生成雪花id

        equipment.setCreateTime(LocalDateTime.now());

        equipment.setCreateId(user.getUserId());  //获取当前用户Id

        log.info("Service层添加器材的信息:{}",equipment);
        equipmentMapper.addEquipment(equipment);

        categoryMapper.AddTotal(equipment.getCategoryId());

    }

    /**
     * 修改器材信息
     * @param equipmentDTO
     */
    @Override
    public void updateEquipment(EquipmentDTO equipmentDTO,UserConstant userConstant) {

        Equipment equipment = new Equipment();

        BeanUtils.copyProperties(equipmentDTO,equipment);

        //更新修改时间
        equipment.setModifiedTime(LocalDateTime.now());

        //设置修改用户Id
        equipment.setModifiedId(userConstant.getUserId());

        equipmentMapper.updateEquipment(equipment);

    }


    /**
     * 根据Id删除器材
     * @param equipmentId
     */
    @Override
    public void deleteEquipment(Long equipmentId,UserConstant userConstant) {

        Long userId = userConstant.getUserId();

        if (equipmentId !=null){
            equipmentMapper.delete(equipmentId,userId);
        }

    }
}
