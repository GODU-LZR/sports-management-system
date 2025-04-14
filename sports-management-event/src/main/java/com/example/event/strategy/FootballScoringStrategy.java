package com.example.event.strategy;

// 足球计分策略实现
public class FootballScoringStrategy implements ScoringStrategy {
    @Override
    public boolean calculateScore(Long matchId) {
        // 足球特有的计分逻辑
        return true;
    }
}
