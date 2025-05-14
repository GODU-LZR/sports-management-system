package com.example.equipment.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.constant.UserConstant;
import com.example.equipment.mapper.sqlProvider.EquipmentSqlProvider;
import com.example.equipment.pojo.Equipment;
import com.example.equipment.pojo.JudgeDamage;
import com.example.equipment.vo.EquipmentVO;
import org.apache.ibatis.annotations.*;
import com.example.equipment.pojo.Equipment; // 导入你的 Equipment 实体类
import com.example.equipment.dto.utilDTO.EquipmentPageQuery; // 导入你的查询 DTO
import com.example.equipment.vo.EquipmentVO; // 导入你的 VO 类

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {

    /*
    新增一个器材
     */
    @Insert("insert into equipment (equipment_id, category_id, picture_url, specification, create_time, modified_time, create_id, modified_id) " +
            "VALUES(#{equipmentId},#{categoryId},#{pictureUrl},#{specification},#{createTime},#{modifiedTime},#{createId},#{modifiedId}) ")
    void addEquipment(Equipment equipment);


    /**
     * 更新器材信息
     * @param equipment
     */
    @Update("update equipment set category_id = #{categoryId},specification = #{specification},picture_url = #{pictureUrl}" +
            " where equipment_id = #{equipmentId} ")
    void updateEquipment(Equipment equipment);


    /**
     * 删除一个器材 将is_deleted设置为1
     * @param equipmentId
     * @param userId
     */
    @Update("update equipment SET is_deleted = 1,modified_id = #{userId},modified_time =now() where equipment.status!=0 and equipment_id = #{equipmentId}")
    void delete(Long equipmentId, Long userId);

    /**
     * 根据器材Id将状态设置为0   即已租用
     * @param equipmentId
     */
    @Update("update equipment set status = 0 where equipment_id = #{equipmentId}")
    void setEquipment_Status_To_0(Long equipmentId);


    @Update("update equipment set status = 1 where equipment_id = #{equipmentId}")
    void setEquipment_Status_To_1(Long equipmentId);

    @Select("select status from equipment where equipment_id = #{equipmentId}")
    Integer ReturnStatus(Long equipmentId);


    /**
     * 自定义分页查询器材列表，包含分类信息
     * 使用 @SelectProvider 动态构建 SQL
     *
     * @param page MyBatis-Plus 自动处理的分页对象
     * @param query 查询条件 DTO
     * @return 包含 EquipmentVO 列表的分页结果
     */
    @SelectProvider(type = EquipmentSqlProvider.class, method = "selectEquipmentVOPage")
    IPage<EquipmentVO> selectEquipmentVOPage(IPage<EquipmentVO> page, @Param("query") EquipmentPageQuery query);

    @Select("select * from equipment where category_id = #{categoryId}")
    List<Equipment> selectByCategoryId(Long categoryId);

    @Select("select count(*) from equipment where status = 1 and category_id = #{categoryId}")
    Integer selectUseableCount(Long categoryId);
    // 注意：
    // 1. 第一个参数是 IPage<EquipmentVO>，MyBatis-Plus 分页拦截器会识别它。
    // 2. 返回类型是 IPage<EquipmentVO>。
    // 3. 使用 @Param("query") 注解，这样在 SQL Provider 中可以通过 #{query.fieldName} 访问 DTO 的属性。
    // 4. 方法名 selectEquipmentVOPage 需要与 SQL Provider 中的方法名一致。



//    IPage<EquipmentVO> selectPage(Page page, Object o);

    /**
     * 查找指定分类下，状态为可用，且在给定时间段内没有已通过或审核中借用申请的器材ID列表。
     * 使用 NOT EXISTS 子查询来排除已被占用的器材。
     *
     * @param categoryId 器材分类ID
     * @param startTime 用户请求的开始时间
     * @param endTime 用户请求的结束时间
     * @param limit 限制返回的数量 (即用户请求的数量)
     * @return 可用的器材ID列表
     */
    @Select("select equipment_id from equipment" +
            " where category_id = #{categoryId} " +
//            "and status = 1 " +
            "and is_deleted = 0 " +
            "and not exists(select 1 from equipment_request er " +
            "where er.equipment_id =equipment.equipment_id " +
            "and er.is_revoked = 0 " +
            "and er.status IN(0,1)" +
            "and er.start_time <#{endTime} and er.end_time >#{startTime}) " +
            "limit #{limit} " +
            "for update")//增加一个悲观锁 避免用户高并发的问题
            //通过在查询可用器材时就锁定这些器材，可以防止其他事务在当前事务完成预订之前修改或预订同一个器材。
    List<Long> findAvailableEquipmentIds(@Param("categoryId") Long categoryId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime,
                                         @Param("limit") int limit);

    @Update("update equipment set condition_score = #{conditionScore} where equipment_id = #{equipmentId}")
    void updateEquipmentCondition(JudgeDamage judgeDamage);


}
