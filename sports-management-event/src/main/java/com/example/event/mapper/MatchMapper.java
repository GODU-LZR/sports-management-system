package com.example.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.event.dao.Match;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 比赛数据访问接口
 */
@Mapper
public interface MatchMapper extends BaseMapper<Match> {
    
    /**
     * 根据赛事ID获取比赛列表
     * 
     * @param gameId 赛事ID
     * @return 比赛列表
     */
    @Select("SELECT * FROM match WHERE game_id = #{gameId} ORDER BY start_time")
    List<Match> getMatchesByGameId(@Param("gameId") Long gameId);
    
    /**
     * 根据比赛ID获取比赛详情
     * 
     * @param matchId 比赛ID
     * @return 比赛详情
     */
    @Select("SELECT * FROM match WHERE match_id = #{matchId}")
    Match getMatchById(@Param("matchId") String matchId);
    
    /**
     * 检查用户是否关注了比赛
     * 
     * @param userId 用户ID
     * @param matchId 比赛ID
     * @return 关注状态（1-已关注，0-未关注）
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE user_id = #{userId} AND match_id = #{matchId}")
    int checkUserFollowStatus(@Param("userId") Long userId, @Param("matchId") String matchId);
    
    /**
     * 获取我创建的赛事下的比赛列表
     * 
     * @param gameId 赛事ID
     * @param userId 用户ID
     * @return 比赛列表
     */
    @Select("SELECT m.* FROM match m JOIN game g ON m.game_id = g.game_id " +
           "WHERE m.game_id = #{gameId} AND g.creator_id = #{userId} ORDER BY m.start_time")
    List<Match> getMyMatchesByGameId(@Param("gameId") Long gameId, @Param("userId") Long userId);
}