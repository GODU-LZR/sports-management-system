package com.example.finance.service.impl;

import com.example.finance.mapper.OrderMapper;
import com.example.finance.pojo.entity.Order;
import com.example.finance.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public boolean save(Order entity) {
        return orderMapper.insert(entity) > 0;
    }

    @Override
    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public Page<Order> page(Page<Order> page, QueryWrapper<Order> queryWrapper) {
        return orderMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean updateById(Order entity) {
        return orderMapper.updateById(entity) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return orderMapper.deleteById(id) > 0;
    }
}