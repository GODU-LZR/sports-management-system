package com.example.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.equipment.dto.EquipmentDTO;
import com.example.equipment.dto.utilDTO.EquipmentPageQuery;
import com.example.equipment.mapper.CategoryMapper;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.pojo.Equipment;
import com.example.equipment.service.EquipmentService;
import com.example.equipment.vo.EquipmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    public IPage<EquipmentVO> PageSelect(EquipmentPageQuery query) {

        // 1. 创建分页对象
        IPage<EquipmentVO> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 2. 创建查询 Wrapper
        // 使用 LambdaQueryWrapper 更安全，避免写错字段名字符串
        LambdaQueryWrapper<EquipmentVO> queryWrapper = new LambdaQueryWrapper<>();

        // 3. 添加模糊查询条件
        // 判断查询关键字是否不为空，如果不为空则添加 LIKE 条件
        if (StringUtils.hasText(query.getSpecification())) {
            // EquipmentVO::getName 是通过方法引用指定要查询的字段
            // query.getName() 是查询的值
            // like() 方法会自动在值两端加上 '%'，生成 SQL 类似 WHERE name LIKE '%keyword%'
            queryWrapper.like(EquipmentVO::getSpecification, query.getSpecification());
        }

        // 如果有其他模糊查询字段，可以类似添加，例如：
        // if (StringUtils.hasText(query.getType())) {
        //     // 如果希望是 OR 关系，可以使用 .or()
        //     queryWrapper.or().like(EquipmentVO::getType, query.getType());
        // }
        // 如果希望是 AND 关系，直接继续添加 like() 即可
        // if (StringUtils.hasText(query.getLocation())) {
        //     queryWrapper.like(EquipmentVO::getLocation, query.getLocation());
        // }


        // 4. 执行分页查询，将分页对象和 Wrapper 对象都传进去
        IPage<EquipmentVO> resultPage = equipmentMapper.selectPage(page, queryWrapper);

        return resultPage;
    }



}
