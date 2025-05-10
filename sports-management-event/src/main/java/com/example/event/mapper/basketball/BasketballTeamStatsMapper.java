package com.example.event.mapper.basketball;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.event.dao.basketball.BasketballTeamStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BasketballTeamStatsMapper  {
    // 根据复合主键查询
    BasketballTeamStats selectByPrimaryKey(
            @Param("matchId") String matchId,
            @Param("teamId") Long teamId
    );

    // 插入数据
    int insert(BasketballTeamStats record);

    // 更新数据
    int updateByPrimaryKey(BasketballTeamStats record);

    // 删除数据
    int deleteByPrimaryKey(
            @Param("matchId") String matchId,
            @Param("teamId") Long teamId
    );
    
    // 获取比赛统计数据
    List<BasketballTeamStats> selectList(@Param("ew") QueryWrapper<BasketballTeamStats> queryWrapper);
}
