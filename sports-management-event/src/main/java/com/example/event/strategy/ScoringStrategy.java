package com.example.event.strategy;

/**
 * 计分策略接口
 */
public interface ScoringStrategy {
    /**
     * 计算比赛得分
     * @param matchId 比赛ID
     * @return 计算结果
     */
    boolean calculateScore(Long matchId);
}

