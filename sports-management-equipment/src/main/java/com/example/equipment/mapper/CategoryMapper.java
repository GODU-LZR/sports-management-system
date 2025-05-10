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
    @Insert("insert into equipment_category (category_id, name, description, value,create_time, modified_time, create_id, modified_id) VALUES " +
            "(#{categoryId},#{name},#{description},#{value},#{createTime},#{modifiedTime},#{createId},#{modifiedId})")
    void addCategory(EquipmentCategory category);

    @Update("update equipment_category set total = total+1 , stock = stock +1,book_stock = book_stock+1 where category_id = #{categoryId}")
    void AddTotal(Long categoryId);

    @Select("select * from equipment_category")
    List<CategoryVO> selectAll();

    /**
     * 更新器材分类
     * @param
     */
    @Update("update equipment_category set name = #{name}," +
            "description = #{description}," +
            "value = #{value}," +
            "modified_id = #{modifiedId}," +
            "modified_time = #{modifiedTime}" +
            " where category_id = #{categoryId}")
    void update(EquipmentCategory equipmentCategory);


    /**
     * 租借器材
     * 通过器材Id 从器材表里 查询对应的器材分类Id 再对物理库存操作数量
     * @param equipmentId
     */
    @Update("update equipment_category ec " +
            "set ec.stock = ec.stock -1 " +
            " where ec.category_id = " +
            "(select category_id from equipment where equipment_id = #{equipmentId})")
    void BorrowEqp(Long equipmentId);



    @Update("update equipment_category ec set ec.stock = stock +1 ,ec.book_stock = book_stock +1 where ec.category_id = (select  category_id from equipment where equipment_id = #{equipmentId})")
    void ReturnEqp(Long equipmentId);

    @Select("select * from equipment_category where name = #{name}")
    EquipmentCategory selectByName(String name);


    //将器材分类表里的账面库存减一
    @Update("update equipment_category ec set ec.book_stock = ec.book_stock - 1 where ec.category_id = (select category_id from equipment where equipment_id = #{equipmenntId})")
    void reduceBookStock(Long equipmentId);

    @Update("update equipment_category ec set ec.book_stock = ec.book_stock +1 where ec.category_id = (select category_id from equipment where equipment_id = #{equipment})")
    void raiseBookStock(Long equipmentId);

//    @Update("update equipment_category ec set ec.total = ec.stock - 1 ,ec.stock = ec.stock - 1")
//    void reduceEquipment(Long equipmentId);
}
