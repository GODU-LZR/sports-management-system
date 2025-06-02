package com.example.venue.mapper;

import com.example.venue.dto.*;
import com.example.venue.pojo.mysql.Order;
import com.example.venue.vo.OrderDetail;
import com.example.venue.vo.OrderPage;
import com.example.venue.vo.ReplaceVenue;
import com.example.venue.vo.TimeOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    List<OrderDetail> listOrder(UserOrderQueryRequest userOrderQueryRequest);

    List<OrderDetail> listOrderByAdmin(AdminOrderQueryRequest adminOrderQueryRequest);

    Integer listOrderByAdminPage(AdminOrderQueryRequest adminOrderQueryRequest);

    int addOrder(OrderAddDto orderAddDto);

    List<TimeOption> listDisabledRange(OrderTimeOptionRequest orderTimeOptionRequest);

    Order getOrderById(String orderId);

    // 根据传入的orderId,返回订单的场地类型、租借开始时间、租借结束时间。用于查找可替换的场地
    ReplaceVenueDto getReplaceData(String orderId);

    // 查找所有符合平替条件的场地
    List<ReplaceVenue> listReplaceVenue(ReplaceVenueDto replaceVenueDto);

    // 将替换的场地,添加到订单中
    int replaceOrder(OrderAddDto orderAddDto);

    int replaceOldOrder(String orderId);

    int agreeOrder(@Param("orderId")String orderId, @Param("auditId") String auditId);

    int disagreeOrder(OrderReasonRequest orderReasonRequest);

    int cancelOrder(OrderReasonRequest orderReasonRequest);

}
