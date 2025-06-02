package com.example.venue.service;

import com.example.venue.dto.*;
import com.example.venue.vo.OrderDetail;
import com.example.venue.vo.OrderPage;
import com.example.venue.vo.ReplaceVenue;
import com.example.venue.vo.TimeOption;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface OrderService {

    List<OrderDetail> listOrder(UserOrderQueryRequest userOrderQueryRequest);

    OrderPage listOrderByAdmin(AdminOrderQueryRequest adminOrderQueryRequest);

    boolean addOrder(OrderAddDto orderAddDto);

    List<TimeOption> listDisabledRange(OrderTimeOptionRequest orderTimeOptionRequest);

    List<ReplaceVenue> listReplaceVenue(String orderId);

    boolean replaceOrder(OrderAddDto orderAddDto);

    boolean agreeOrder(String orderId, String auditId);

    boolean disagreeOrder(OrderReasonRequest orderReasonRequest);

    boolean cancelOrder(OrderReasonRequest orderReasonRequest);
}
