package com.example.equipment.mapper;


import com.example.equipment.dto.EquipmentDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EquipmentMapper {

    @Insert("insert into equipment(equipment_id, equipment_name, picture_url, value, total, stock, create_time,create_id) " +
            "VALUES(#{equipmentId},#{equipmentName},#{pictureUrl},#{value},#{total},#{stock},#{createTime},#{createId}) ")
    void addEquipment(EquipmentDTO equipmentDTO);


    void updateEquipment(EquipmentDTO equipmentDTO);


    @Update("update equipment SET is_deleted = 1 where equipment_id = #{equipmentId}")
    void delete(Long equipmentId);
}
