package com.example.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.finance.pojo.entity.Order;
import com.example.finance.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    // 插入订单，默认使用 ORDER-0001 的结构
    @PostMapping
    public String addOrder(


            @RequestParam(value = "userId", defaultValue = "1") Long userId,
            @RequestParam(value = "userName", defaultValue = "测试") String userName,
            @RequestParam(value = "orderType", defaultValue = "1") Integer orderType,
            @RequestParam(value = "orderStatus", defaultValue = "60") Integer orderStatus,
            @RequestParam(value = "paymentStatus", defaultValue = "1") Integer paymentStatus,
            @RequestParam(value = "totalAmount", defaultValue = "200.00") BigDecimal totalAmount,
            @RequestParam(value = "paidAmount", defaultValue = "200.00") BigDecimal paidAmount,
            @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
            @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod,
            @RequestParam(value = "paymentTime", defaultValue = "2025-05-10 12:30:00") String paymentTimeString,
            @RequestParam(value = "transactionId", defaultValue = "TRANS-0001") String transactionId,
            @RequestParam(value = "remark", defaultValue = "无") String remark,
            @RequestParam(value = "createTime", defaultValue = "2025-05-10 10:30:00") String createTimeString,
            @RequestParam(value = "updateTime", defaultValue = "2025-05-10 12:30:00") String updateTimeString
    ) {

        String orderNo="ORDER"+snowflakeIdGenerator.nextId();
        Order order = new Order();

        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setUserName(userName);
        order.setOrderType(com.example.finance.enums.OrderType.fromCode(orderType));
        order.setOrderStatus(com.example.common.enums.OrderAmountStatus.fromCode(orderStatus));
        order.setPaymentStatus(paymentStatus);
        order.setTotalAmount(totalAmount);
        order.setPaidAmount(paidAmount);
        order.setRefundAmount(refundAmount);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.parse(paymentTimeString, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        order.setTransactionId(transactionId);
        order.setRemark(remark);
        order.setCreateTime(LocalDateTime.parse(createTimeString, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        order.setUpdateTime(LocalDateTime.parse(updateTimeString, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        boolean success = orderService.save(order);
        return success ? "添加成功" : "添加失败";
    }

    // 查询单个订单（带默认ID）
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable(value = "id") Long id) {
        return orderService.getById(id);
    }

    // 查询所有订单（分页）
    @GetMapping
    public Page<Order> getAllOrders(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<Order> pageInfo = new Page<>(page, size);
        return orderService.page(pageInfo, new QueryWrapper<>());
    }

    // 更新订单（带默认值）
    @PutMapping
    public String updateOrder(
            @RequestParam(value = "id", defaultValue = "134567890123456789") Long id,
            @RequestParam(value = "orderNo", defaultValue = "ORDER-UPDATE") String orderNo,
            @RequestParam(value = "userId", defaultValue = "1") Long userId,
            @RequestParam(value = "userName", defaultValue = "张三更新版") String userName,
            @RequestParam(value = "orderType", defaultValue = "1") Integer orderType,
            @RequestParam(value = "orderStatus", defaultValue = "60") Integer orderStatus,
            @RequestParam(value = "paymentStatus", defaultValue = "1") Integer paymentStatus,
            @RequestParam(value = "totalAmount", defaultValue = "200.00") BigDecimal totalAmount,
            @RequestParam(value = "paidAmount", defaultValue = "200.00") BigDecimal paidAmount,
            @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
            @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod,
            @RequestParam(value = "paymentTime", defaultValue = "2025-05-10 12:30:00") String paymentTimeString,
            @RequestParam(value = "transactionId", defaultValue = "TRANS-0001") String transactionId,
            @RequestParam(value = "remark", defaultValue = "已更新") String remark,
            @RequestParam(value = "createTime", defaultValue = "2025-05-10 10:30:00") String createTimeString,
            @RequestParam(value = "updateTime", defaultValue = "2025-05-11 12:30:00") String updateTimeString
    ) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setUserName(userName);
        order.setOrderType(com.example.finance.enums.OrderType.fromCode(orderType));
        order.setOrderStatus(com.example.common.enums.OrderAmountStatus.fromCode(orderStatus));
        order.setPaymentStatus(paymentStatus);
        order.setTotalAmount(totalAmount);
        order.setPaidAmount(paidAmount);
        order.setRefundAmount(refundAmount);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.parse(paymentTimeString, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        order.setTransactionId(transactionId);
        order.setRemark(remark);
        order.setCreateTime(LocalDateTime.parse(createTimeString, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        order.setUpdateTime(LocalDateTime.parse(updateTimeString, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        boolean success = orderService.updateById(order);
        return success ? "更新成功" : "更新失败";
    }

    // 删除订单（带默认ID）
    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable("id") Long id) {
        boolean success = orderService.removeById(id);
        return success ? "删除成功" : "删除失败";
    }
}