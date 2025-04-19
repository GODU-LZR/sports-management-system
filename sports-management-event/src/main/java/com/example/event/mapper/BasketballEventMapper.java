package com.example.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.event.entity.BasketballEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 篮球赛事数据访问接口
 */
@Mapper
public interface BasketballEventMapper extends BaseMapper<BasketballEvent> {
    // 继承BaseMapper后默认拥有基础的CRUD方法
    // 可以在这里添加自定义的查询方法
}