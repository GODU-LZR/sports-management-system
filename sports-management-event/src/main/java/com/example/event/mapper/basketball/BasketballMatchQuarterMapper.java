package com.example.event.mapper.basketball;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.event.dao.basketball.BasketballMatchQuarter;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface BasketballMatchQuarterMapper extends BaseMapper<BasketballMatchQuarter> {
    @Select("SELECT  match_id,team_id,q1,q2,q3,q4 FROM event_basketball_match_quarters where match_id =#{matchId}")
    @Results({
            @Result(property = "id.matchId",column = "match_id"),
            @Result(property = "id.teamId",column = "team_id")
    })
    List<BasketballMatchQuarter> selectByMatchId(@Param("matchId") String matchId);


}
