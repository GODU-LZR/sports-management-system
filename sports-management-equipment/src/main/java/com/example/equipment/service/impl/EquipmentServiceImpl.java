package com.example.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import java.util.List;

import static java.lang.Math.random;


@Service
@Slf4j
public class EquipmentServiceImpl  extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

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
//          Equipment equipment = equipmentMapper.find_IS_used(equipmentId);

            equipmentMapper.delete(equipmentId,userId);

//            categoryMapper.reduceEquipment(equipmentId);

        }

    }

    /**
     * 分页查询器材列表 (返回 EquipmentVO)
     * 直接调用 Mapper 中自定义的 selectEquipmentVOPage 方法
     * @param query 查询条件和分页信息
     * @return 分页结果 IPage<EquipmentVO>
     */
    @Override
    public IPage<EquipmentVO> PageSelect(EquipmentPageQuery query) {

        // 1. 创建分页对象
        // MyBatis-Plus 会根据这个对象中的 current 和 size 进行分页
        // 泛型参数 EquipmentVO 匹配了 Mapper 方法的返回类型 IPage<EquipmentVO>
        IPage<EquipmentVO> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 2. 直接调用 EquipmentMapper 中自定义的 selectEquipmentVOPage 方法
        // 这个方法已经在 Mapper XML 或 @SelectProvider 中写好了 JOIN 和 WHERE 条件逻辑
        // 它接收 IPage<EquipmentVO> 和 EquipmentPageQuery DTO
        // Mapper 方法会负责根据 query DTO 中的条件（如 specification）构建 WHERE 子句
        log.info("Service层执行分页查询，查询条件:{}", query);
        IPage<EquipmentVO> resultPage = equipmentMapper.selectEquipmentVOPage(page, query);

        log.info("Service层分页查询结果:{}", resultPage.getRecords().size()); // 记录查询到的条数
        return resultPage;
    }

}
