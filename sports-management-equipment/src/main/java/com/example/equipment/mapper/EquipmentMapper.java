package com.example.equipment.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.constant.UserConstant;
import com.example.equipment.mapper.sqlProvider.EquipmentSqlProvider;
import com.example.equipment.pojo.Equipment;
import com.example.equipment.vo.EquipmentVO;
import org.apache.ibatis.annotations.*;
import com.example.equipment.pojo.Equipment; // 导入你的 Equipment 实体类
import com.example.equipment.dto.utilDTO.EquipmentPageQuery; // 导入你的查询 DTO
import com.example.equipment.vo.EquipmentVO; // 导入你的 VO 类

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
    @Update("update equipment SET is_deleted = 1,modified_id = #{userId},modified_time =now() where equipment_id = #{equipmentId}")
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
    // 注意：
    // 1. 第一个参数是 IPage<EquipmentVO>，MyBatis-Plus 分页拦截器会识别它。
    // 2. 返回类型是 IPage<EquipmentVO>。
    // 3. 使用 @Param("query") 注解，这样在 SQL Provider 中可以通过 #{query.fieldName} 访问 DTO 的属性。
    // 4. 方法名 selectEquipmentVOPage 需要与 SQL Provider 中的方法名一致。

//    IPage<EquipmentVO> selectPage(Page page, Object o);
}
