package com.example.event.mapper.basketball;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.event.dao.basketball.BasketballShotChart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 篮球投篮图 Mapper 接口
 */
@Mapper
public interface BasketballShotChartMapper extends BaseMapper<BasketballShotChart> {
    // 在这里可以定义自定义的 SQL 方法，如果 BaseMapper 提供的基本方法不能满足需求
}