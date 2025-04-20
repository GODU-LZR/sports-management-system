package com.example.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.event.entity.BasketballPlayerStats;
import org.apache.ibatis.annotations.Mapper;

/**
 * 篮球球员统计数据访问接口
 */
@Mapper
public interface BasketballPlayerStatsMapper extends BaseMapper<BasketballPlayerStats> {
    // 继承BaseMapper后默认拥有基础的CRUD方法
    // 可以在这里添加自定义的查询方法
}