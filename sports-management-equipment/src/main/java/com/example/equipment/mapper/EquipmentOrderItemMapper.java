package com.example.equipment.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.equipment.constant.OrderEquipmentStatusConstant;
import com.example.equipment.pojo.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EquipmentOrderItemMapper extends BaseMapper<OrderItem> {


    /**
     * 查询那些未使用和未领取的
     *
     * @param equipmentId
     * @return
     */
    @Select("select * from equipment_borrow_detail where item_status_id in (1,2) and equipment_id = #{equipmentId}")
    OrderItem selectByEquipmentId(Long equipmentId);

    //将器材订单表中的状态设置为已借出
    @Update("update equipment_borrow_detail set item_status_id = #{statusId} where detail_id = #{datailId}")
    void updateOrderStatus(@Param("statusId") Integer statusId ,Long detailId);


//    @Update("UPDATE equipment_borrow_detail SET item_status_id = #{status} WHERE detail_id = #{id}")
//    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

}
