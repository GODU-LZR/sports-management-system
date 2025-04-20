package com.example.event.service;

import com.example.event.DTO.*;
import java.util.List;

/**
 * 比赛数据服务接口
 * 提供获取比赛相关数据的方法
 */
public interface MatchDataService {
    
    /**
     * 获取比赛概览数据
     * @param matchId 比赛ID
     * @return 比赛概览数据列表
     */
    List<TeamSummary> getSummary(String matchId);
    
    /**
     * 获取分节得分数据
     * @param matchId 比赛ID
     * @return 分节得分数据列表
     */
    List<TeamQuarters> getQuarterScores(String matchId);
    
    /**
     * 获取球员高光数据
     * @param matchId 比赛ID
     * @return 球员高光数据
     */
    HighlightsData getHighlights(String matchId);
    
    /**
     * 获取全队综合统计数据
     * @param matchId 比赛ID
     * @return 全队综合统计数据列表
     */
    List<TeamStatistics> getTeamStats(String matchId);
    
    /**
     * 获取投篮热图数据
     * @param matchId 比赛ID
     * @return 投篮热图数据
     */
    ShotChart getShotChart(String matchId);
}