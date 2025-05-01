package com.example.equipment.mapper;


import com.example.equipment.pojo.EquipmentCategory;
import com.example.equipment.vo.CategoryVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CategoryMapper {


    /**
     * 插入一条器材分类的数据
     *
     * @param category
     */
    @Insert("insert into equipment_category (category_id, name, description, value, total, stock, create_time, modified_time, create_id, modified_id) VALUES " +
            "(#{categoryId},#{name},#{description},#{value},#{total},#{stock},#{createTime},#{modifiedTime},#{createId},#{modifiedId})")
    void addCategory(EquipmentCategory category);

    @Update("update equipment_category set total = total+1 where category_id = #{categoryId}")
    void AddTotal(Long categoryId);

    @Select("select * from equipment_category")
    List<CategoryVO> selectAll();


    /**
     * 更新器材分类
     * @param
     */
    @Update("update equipment_category set name = #{name}," +
            "description = #{description},stock = #{stock}" +
            ",value = #{value},total = #{total}," +
            "modified_id = #{modifiedId}," +
            "modified_time = #{modifiedTime}" +
            " where category_id = #{categoryId}")
    void update(EquipmentCategory equipmentCategory);

    /**
     * 通过器材Id 从器材表里 查询对应的器材分类Id 再对库存操作数量
     * @param equipmentId
     */
    @Update("update equipment_category ec set ec.stock = stock -1 where ec.category_id = (select category_id from equipment where equipment_id = #{equipmentId})")
    void BorrowEqp(Long equipmentId);



    @Update("update equipment_category ec set ec.stock = stock +1 where ec.category_id = (select  category_id from equipment where equipment_id = #{equipmentId})")
    void ReturnEqp(Long equipmentId);
}
