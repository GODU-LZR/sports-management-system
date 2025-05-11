package com.example.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.equipment.pojo.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EquipmentOrderMapper extends BaseMapper<Order> {


//    @Insert("insert into equipment_borrow_order")
//    void insertOrder(Order order);
}
