package com.example.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.finance.pojo.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 * 基于MyBatis Plus实现订单表的数据库操作
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    // 继承BaseMapper后默认已经有基础的CRUD方法
    // 可以在这里添加自定义的查询方法
}