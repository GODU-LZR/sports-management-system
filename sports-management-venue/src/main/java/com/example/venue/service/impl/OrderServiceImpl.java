package com.example.venue.service.impl;

import com.example.venue.dto.*;
import com.example.venue.mapper.OrderMapper;
import com.example.venue.service.OrderService;
import com.example.venue.vo.OrderDetail;
import com.example.venue.vo.OrderPage;
import com.example.venue.vo.ReplaceVenue;
import com.example.venue.vo.TimeOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<OrderDetail> listOrder(UserOrderQueryRequest userOrderQueryRequest) {
        return orderMapper.listOrder(userOrderQueryRequest);
    }

    @Override
    public OrderPage listOrderByAdmin(AdminOrderQueryRequest adminOrderQueryRequest) {
        OrderPage orderPage = new OrderPage();
        List<OrderDetail> orderDetails = orderMapper.listOrderByAdmin(adminOrderQueryRequest);
        Integer total = orderMapper.listOrderByAdminPage(adminOrderQueryRequest);
        orderPage.setTotal(total);
        orderPage.setData(orderDetails);
        return orderPage;
    }

    @Override
    public boolean addOrder(OrderAddDto orderAddDto) {
        int rowsAffected = orderMapper.addOrder(orderAddDto);
        return rowsAffected > 0;
    }

    @Override
    public List<TimeOption> listDisabledRange(OrderTimeOptionRequest orderTimeOptionRequest) {
        return orderMapper.listDisabledRange(orderTimeOptionRequest);
    }

    // 需要使用orderId查出数据,然后找到对应赛事类型,且时间在同一天的当前时间或后面时间的选项值。
    @Override
    public List<ReplaceVenue> listReplaceVenue(String orderId) {
        // 获取进行更换场地所需要的数据
        ReplaceVenueDto replaceVenueDto = orderMapper.getReplaceData(orderId);
        if(replaceVenueDto == null) {
            return null;
        }
        // 如果当前时间晚于租借开始时间,那么就需要从下一个30分钟段开始寻找合适的条件
        boolean isAfter = LocalDateTime.now().isAfter(replaceVenueDto.getStartTime());
        if(isAfter) {
            replaceVenueDto.setStartTime(replaceVenueDto.getStartTime().plusMinutes(30));
        }
        // 如果增加后，开始时间和结束时间一致,则无需寻找，直接返回一个空列表即可
        if(replaceVenueDto.getStartTime().isEqual(replaceVenueDto.getEndTime())){
            return Collections.emptyList();
        }
        List<ReplaceVenue> list = orderMapper.listReplaceVenue(replaceVenueDto);
        list = list.stream()
                .peek(venue -> {
                    venue.setStartTime(replaceVenueDto.getStartTime());
                    venue.setEndTime(replaceVenueDto.getEndTime());
                })
                .collect(Collectors.toList());
        return list;
    }

    // 新增订单:更换场地时的订单
    @Override
    @Transactional
    public boolean replaceOrder(OrderAddDto orderAddDto) {
        int rowsAffected1 = orderMapper.replaceOldOrder(orderAddDto.getOrderId());
        if(rowsAffected1 <= 0) {
            return false;
        }
        int rowsAffected2 = orderMapper.replaceOrder(orderAddDto);
        return rowsAffected2 > 0;
    }

    @Override
    public boolean agreeOrder(String orderId, String auditId) {
        int rowsAffected = orderMapper.agreeOrder(orderId, auditId);
        return rowsAffected > 0;
    }

    @Override
    public boolean disagreeOrder(OrderReasonRequest orderReasonRequest) {
        int rowsAffected = orderMapper.disagreeOrder(orderReasonRequest);
        return rowsAffected > 0;
    }

    @Override
    public boolean cancelOrder(OrderReasonRequest orderReasonRequest) {
        int rowsAffected = orderMapper.cancelOrder(orderReasonRequest);
        return rowsAffected > 0;
    }
}
