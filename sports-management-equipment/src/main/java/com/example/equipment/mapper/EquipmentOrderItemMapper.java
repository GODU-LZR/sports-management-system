package com.example.equipment.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.equipment.pojo.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EquipmentOrderItemMapper extends BaseMapper<OrderItem> {


//    @Insert("insert into equipment_borrow_detail")
//    void insertOrderItem(OrderItem orderItem);
}
