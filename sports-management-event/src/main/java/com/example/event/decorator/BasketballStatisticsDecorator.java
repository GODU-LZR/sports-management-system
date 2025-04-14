package com.example.event.decorator;

import com.example.event.entity.SportEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 篮球赛事统计数据装饰器
 * 用于为篮球赛事添加统计数据功能
 */
@Getter
@Setter
public class BasketballStatisticsDecorator extends EventDecorator {
    
    /**
     * 球队得分统计 (队伍ID -> 得分)
     */
    private Map<Long, Integer> teamScores = new HashMap<>();
    
    /**
     * 球员得分统计 (球员ID -> 得分)
     */
    private Map<Long, Integer> playerScores = new HashMap<>();
    
    /**
     * 球员篮板统计 (球员ID -> 篮板数)
     */
    private Map<Long, Integer> playerRebounds = new HashMap<>();
    
    /**
     * 球员助攻统计 (球员ID -> 助攻数)
     */
    private Map<Long, Integer> playerAssists = new HashMap<>();
    
    /**
     * 球员抢断统计 (球员ID -> 抢断数)
     */
    private Map<Long, Integer> playerSteals = new HashMap<>();
    
    /**
     * 球员盖帽统计 (球员ID -> 盖帽数)
     */
    private Map<Long, Integer> playerBlocks = new HashMap<>();
    
    public BasketballStatisticsDecorator(SportEvent decoratedEvent) {
        super(decoratedEvent);
    }
    
    /**
     * 记录球员得分
     * @param playerId 球员ID
     * @param points 得分 (2分或3分)
     */
    public void recordPlayerScore(Long playerId, Integer points) {
        playerScores.put(playerId, playerScores.getOrDefault(playerId, 0) + points);
        
        // 更新球队得分
        // 这里需要根据实际业务逻辑获取球员所属队伍ID
        // Long teamId = getPlayerTeamId(playerId);
        // teamScores.put(teamId, teamScores.getOrDefault(teamId, 0) + points);
    }
    
    /**
     * 记录球员篮板
     * @param playerId 球员ID
     */
    public void recordRebound(Long playerId) {
        playerRebounds.put(playerId, playerRebounds.getOrDefault(playerId, 0) + 1);
    }
    
    /**
     * 记录球员助攻
     * @param playerId 球员ID
     */
    public void recordAssist(Long playerId) {
        playerAssists.put(playerId, playerAssists.getOrDefault(playerId, 0) + 1);
    }
    
    /**
     * 记录球员抢断
     * @param playerId 球员ID
     */
    public void recordSteal(Long playerId) {
        playerSteals.put(playerId, playerSteals.getOrDefault(playerId, 0) + 1);
    }
    
    /**
     * 记录球员盖帽
     * @param playerId 球员ID
     */
    public void recordBlock(Long playerId) {
        playerBlocks.put(playerId, playerBlocks.getOrDefault(playerId, 0) + 1);
    }
    
    /**
     * 获取球员总得分
     * @param playerId 球员ID
     * @return 总得分
     */
    public Integer getPlayerTotalScore(Long playerId) {
        return playerScores.getOrDefault(playerId, 0);
    }
    
    /**
     * 获取球队总得分
     * @param teamId 队伍ID
     * @return 总得分
     */
    public Integer getTeamTotalScore(Long teamId) {
        return teamScores.getOrDefault(teamId, 0);
    }
    
    @Override
    public String getSportType() {
        return decoratedEvent.getSportType();
    }
}