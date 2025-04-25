package com.example.event.mapper.basketball;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.event.DTO.ShotChart;
import com.example.event.dao.basketball.BasketballShotChart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

/**
 * 篮球投篮图 Mapper 接口
 */
@Mapper
public interface BasketballShotChartMapper extends BaseMapper<BasketballShotChart> {
    ShotChart selectShotChart(@Param("match_id") String matchId);
}