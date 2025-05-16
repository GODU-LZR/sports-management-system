package com.example.finance.service;

import com.example.finance.pojo.entity.Order;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public interface OrderService {

    boolean save(Order entity);

    Order getById(Long id);

    Page<Order> page(Page<Order> page, QueryWrapper<Order> queryWrapper);

    boolean updateById(Order entity);

    boolean removeById(Long id);
}