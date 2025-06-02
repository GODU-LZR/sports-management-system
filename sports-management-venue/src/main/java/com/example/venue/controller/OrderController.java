package com.example.venue.controller;

import com.example.venue.dto.*;
import com.example.venue.service.OrderService;
import com.example.venue.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    public OrderService orderService;

    private final String adminId = "Admin12345678";

    private final String userId = "User12345678";

    // 查询我的订单信息
    @PostMapping("/query")
    public Result<List<OrderDetail>> listOrder(@RequestBody @Valid UserOrderQueryRequest userOrderQueryRequest) {
        userOrderQueryRequest.modifyPage();
        userOrderQueryRequest.setUserId(this.userId);
        log.info("接收到普通用户查询订单请求，参数: {}", userOrderQueryRequest);
        List<OrderDetail> list = orderService.listOrder(userOrderQueryRequest);
        return Result.success(list);
    }

    // (管理员)查询全部订单信息
    @PostMapping("/admin/query")
    public Result<OrderPage> listOrderByAdmin(@RequestBody @Valid AdminOrderQueryRequest adminOrderQueryRequest) {
        adminOrderQueryRequest.modifyPage();
        log.info("接收到管理员查询订单请求，参数: {}", adminOrderQueryRequest);
        OrderPage orderPage = orderService.listOrderByAdmin(adminOrderQueryRequest);
        return Result.success(orderPage);
    }

    // 新增(租借)场地订单信息
    @PostMapping
    public Result<?> addOrder(@RequestBody @Valid OrderAddRequest orderAddRequest) {
        OrderAddDto orderAddDto = OrderAddDto.fromRequest(orderAddRequest);
        String orderId = "Order" + System.currentTimeMillis();
        orderAddDto.setOrderId(orderId);
        orderAddDto.setUserId(this.userId);
        log.info("接收到新增租借订单请求，参数: {}", orderAddDto);
        boolean success = orderService.addOrder(orderAddDto);
        if(success){
            return Result.success();
        }
        return Result.failure("新增场地失败!");
    }

    // 根据日期查询不可用时间段
    @PostMapping("/disabled")
    public Result<List<TimeOption>> listDisabledRange(@RequestBody @Valid OrderTimeOptionRequest orderTimeOptionRequest) {
        log.info("接收到获取已有时间段请求，参数: {}", orderTimeOptionRequest);
        List<TimeOption> list = orderService.listDisabledRange(orderTimeOptionRequest);
        return Result.success(list);
    }

    // 更换场地,获取可选选项
    @GetMapping("/{orderId}/replace")
    public Result<List<ReplaceVenue>> listReplaceVenue(@PathVariable("orderId") String orderId) {
        log.info("接收到更换场地请求，参数: {}", orderId);
        List<ReplaceVenue> list = orderService.listReplaceVenue(orderId);
        return Result.success(list);
    }

    // 更换场地,获取可选选项
    // 本质上再次创建订单,但是state设为4(更换场地)
    @PostMapping("/replace")
    public Result<?> replaceOrder(@RequestBody @Valid OrderReplaceRequest orderReplaceRequest) {
        OrderAddDto orderAddDto = OrderAddDto.fromRequest(orderReplaceRequest);
        log.info("接收到新增更换场地订单请求，参数: {}", orderAddDto);
        boolean success = orderService.replaceOrder(orderAddDto);
        if(success) {
            return Result.success();
        }
        return Result.failure("更换场地失败!");
    }

    // (管理员)通过订单
    @GetMapping("/{orderId}/agree")
    public Result<?> agreeOrder(@PathVariable("orderId") String orderId) {
        log.info("接收到批准场地订单请求，参数: {}", orderId);
        String auditId = "testAudit123";
        boolean flag = orderService.agreeOrder(orderId, auditId);
        if(flag) {
            return Result.success();
        }
        return Result.failure("订单不存在或非待审核状态");
    }

    // (管理员)否决订单
    @PostMapping("/disagree")
    public Result<?> disagreeOrder(@RequestBody @Valid OrderReasonRequest orderReasonRequest) {
        log.info("接收到否决场地订单请求，参数: {}", orderReasonRequest);
        String auditId = "testAudit123";
        orderReasonRequest.setAuditId(auditId);
        boolean success = orderService.disagreeOrder(orderReasonRequest);
        if(success) {
            return Result.success();
        }
        return Result.failure("订单不存在或非待审核状态");
    }

    // (管理员)撤销订单
    @PostMapping("/cancel")
    public Result<?> cancelOrder(@RequestBody @Valid OrderReasonRequest orderReasonRequest) {
        log.info("接收到撤销场地订单请求，参数: {}", orderReasonRequest);
        String auditId = "testAudit123";
        orderReasonRequest.setAuditId(auditId);
        boolean success = orderService.cancelOrder(orderReasonRequest);
        if(success) {
            return Result.success();
        }
        return Result.failure("订单不存在或非已通过状态");
    }

    // 根据关键字获取到,当前用户已租借的场地列表
    @GetMapping("/options")
    public Result<List<VenueOption>> getUserVenueOptions(@RequestParam(value = "key", required = false) String key) {
        return null;
    }
}
