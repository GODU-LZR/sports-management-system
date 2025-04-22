package com.example.event.mapper.basketball;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.event.dao.basketball.BasketballMatchQuarter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
@Mapper
public interface BasketballMatchQuarterMapper extends BaseMapper<BasketballMatchQuarter> {
    @Select("SELECT * FROM event_basketball_match_quarters WHERE match_id = #{matchId}")
    List<BasketballMatchQuarter> selectByMatchId(String matchId);

    // 批量更新（自定义SQL）
    @Update("<script>" +
            "UPDATE event_match_quarters SET " +
            "<foreach collection='list' item='item' separator=','>" +
            "q1=#{item.q1}, q2=#{item.q2}, q3=#{item.q3}, q4=#{item.q4} " +
            "WHERE match_id=#{item.id.matchId} AND team_id=#{item.id.teamId}" +
            "</foreach>" +
            "</script>")
    int batchUpdateQuarters(@Param("list") List<BasketballMatchQuarter> quarters);
}
