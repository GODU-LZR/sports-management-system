package com.example.equipment.mapper;


import com.example.common.constant.UserConstant;
import com.example.equipment.pojo.Equipment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EquipmentMapper {

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
}
