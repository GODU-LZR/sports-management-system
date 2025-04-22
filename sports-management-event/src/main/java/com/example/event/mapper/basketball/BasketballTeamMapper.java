package com.example.event.mapper.basketball;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.event.dao.basketball.BasketballMatch;
import com.example.event.dao.basketball.BasketballTeam;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface BasketballTeamMapper  extends BaseMapper<BasketballTeam> {
    // 自定义查询：根据排名范围查询
    @Select("SELECT * FROM event_team WHERE rank_position BETWEEN #{min} AND #{max}")
    List<BasketballTeam> selectByRankRange(@Param("min") int minRank, @Param("max") int maxRank);

    // 自定义连表查询（带显式字段映射）
    @Results({
            @Result(column = "home_team_id", property = "homeTeamId"),
            @Result(column = "away_team_id", property = "awayTeamId"),
            @Result(column = "home_team_id", property = "homeTeam",
                    one = @One(select = "com.example.mapper.TeamMapper.selectById")),
            @Result(column = "away_team_id", property = "awayTeam",
                    one = @One(select = "com.example.mapper.TeamMapper.selectById"))
    })
    @Select("SELECT * FROM event_match WHERE match_id = #{matchId}")
    BasketballMatch selectMatchWithTeams(String matchId);
}
