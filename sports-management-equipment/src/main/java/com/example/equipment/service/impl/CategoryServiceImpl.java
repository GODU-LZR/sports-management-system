package com.example.equipment.service.impl;


import com.example.common.config.UserConstantArgumentResolver;
import com.example.common.constant.UserConstant;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.equipment.dto.CategoryDTO;
import com.example.equipment.mapper.CategoryMapper;
import com.example.equipment.pojo.EquipmentCategory;
import com.example.equipment.service.CategoryService;
import com.example.equipment.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Math.random;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 新增一个器材的分类  例如篮球类  排球类
     * @param categoryDTO
     */
    @Override
    public void addCateGory(CategoryDTO categoryDTO,UserConstant currentUser) {

        EquipmentCategory category = new EquipmentCategory();

        //拷贝属性
        BeanUtils.copyProperties(categoryDTO,category);

        category.setCreateTime(LocalDateTime.now());  //设置创建时间
        category.setCategoryId(snowflakeIdGenerator.nextId());  //设置器材分类id

        category.setCreateId(currentUser.getUserId());

        log.info("当前的用户id为:{}",category.getCreateId());

        log.info("Service层的器材分类对象为:{}",category);

        categoryMapper.addCategory(category);
     }

    /**
     * 查询所有的器材分类
     * @return
     */
    @Override
    public List<CategoryVO> selectAll() {

       List<CategoryVO> list = categoryMapper.selectAll();

        return list;
    }

    @Override
    public void updateCategory(CategoryDTO categoryDTO, UserConstant currentUser) {

        EquipmentCategory equipmentCategory = new EquipmentCategory();

        BeanUtils.copyProperties(categoryDTO,equipmentCategory);

        equipmentCategory.setModifiedTime(LocalDateTime.now());  //设置修改实现

        equipmentCategory.setModifiedId(currentUser.getUserId());

        log.info("Service层修改器材分类的信息为:{}",equipmentCategory);
        categoryMapper.update(equipmentCategory);

    }
}
