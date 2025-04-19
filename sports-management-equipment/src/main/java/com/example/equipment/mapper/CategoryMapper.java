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



    @Select("select * from equipment_category")
    List<CategoryVO> selectAll();


    /**
     * 更新器材分类
     * @param categoryDTO
     */
    @Update("update equipment_category set name = #{name},description = #{description},stock = #{sotck},value = #{value},total = #{total},modified_id = #{modifiedId},modified_time = #{modifiedTime} where category_id = #{categoryId}")
    void update(EquipmentCategory equipmentCategory);
}
