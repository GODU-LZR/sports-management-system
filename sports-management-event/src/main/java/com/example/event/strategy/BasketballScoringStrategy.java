package com.example.event.strategy;

import com.example.event.decorator.BasketballMatchRecordDecorator;
import com.example.event.decorator.BasketballStatisticsDecorator;
import com.example.event.entity.BasketballEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 篮球计分策略实现
 * 实现篮球特有的计分逻辑
 */
@Component
@RequiredArgsConstructor
public class BasketballScoringStrategy implements ScoringStrategy {
    
    // 比赛得分记录 (临时存储，实际应从数据库获取)
    private final Map<Long, Map<String, Object>> matchScoreRecords = new HashMap<>();
    
    @Override
    public boolean calculateScore(Long matchId) {
        // 从数据库获取比赛信息
        // MatchInfo matchInfo = matchRepository.findById(matchId).orElse(null);
        // if (matchInfo == null) {
        //     return false;
        // }
        
        // 模拟获取比赛所属的赛事
        BasketballEvent basketballEvent = new BasketballEvent();
        
        // 使用装饰器添加比赛记录功能
        BasketballMatchRecordDecorator matchRecordDecorator = 
                new BasketballMatchRecordDecorator(basketballEvent);
        
        // 使用装饰器添加统计数据功能
        BasketballStatisticsDecorator statisticsDecorator = 
                new BasketballStatisticsDecorator(basketballEvent);
        
        // 获取比赛记录
        BasketballMatchRecordDecorator.MatchRecord matchRecord = 
                matchRecordDecorator.getMatchRecord(matchId);
        
        if (matchRecord == null) {
            return false;
        }
        
        // 计算总得分
        calculateTotalScore(matchRecord);
        
        // 更新球员统计数据
        updatePlayerStatistics(matchId, statisticsDecorator);
        
        // 更新比赛状态
        if ("IN_PROGRESS".equals(matchRecord.getStatus()) && isMatchTimeOver(matchRecord)) {
            matchRecordDecorator.endMatch(matchId);
        }
        
        return true;
    }
    
    /**
     * 记录得分事件
     * @param matchId 比赛ID
     * @param playerId 球员ID
     * @param scoreType 得分类型 (TWO_POINTER, THREE_POINTER, FREE_THROW)
     * @return 是否记录成功
     */
    public boolean recordScore(Long matchId, Long playerId, String scoreType) {
        // 获取或创建比赛得分记录
        Map<String, Object> scoreRecord = matchScoreRecords.computeIfAbsent(matchId, k -> new HashMap<>());
        
        // 获取球员得分记录
        @SuppressWarnings("unchecked")
        Map<Long, Map<String, Integer>> playerScores = (Map<Long, Map<String, Integer>>) 
                scoreRecord.computeIfAbsent("playerScores", k -> new HashMap<Long, Map<String, Integer>>());
        
        // 获取或创建球员得分统计
        Map<String, Integer> playerScore = playerScores.computeIfAbsent(playerId, k -> new HashMap<>());
        
        // 根据得分类型更新得分
        int points = 0;
        switch (scoreType) {
            case "TWO_POINTER":
                points = 2;
                playerScore.put("twoPointers", playerScore.getOrDefault("twoPointers", 0) + 1);
                break;
            case "THREE_POINTER":
                points = 3;
                playerScore.put("threePointers", playerScore.getOrDefault("threePointers", 0) + 1);
                break;
            case "FREE_THROW":
                points = 1;
                playerScore.put("freeThrows", playerScore.getOrDefault("freeThrows", 0) + 1);
                break;
            default:
                return false;
        }
        
        // 更新总得分
        playerScore.put("totalPoints", playerScore.getOrDefault("totalPoints", 0) + points);
        
        // 更新比赛总得分
        Integer matchTotalScore = (Integer) scoreRecord.getOrDefault("totalScore", 0);
        scoreRecord.put("totalScore", matchTotalScore + points);
        
        return true;
    }
    
    /**
     * 计算总得分
     */
    private void calculateTotalScore(BasketballMatchRecordDecorator.MatchRecord matchRecord) {
        // 实际实现中，这里应该从数据库中获取所有得分事件并计算
        // 这里使用简化的实现
    }
    
    /**
     * 更新球员统计数据
     */
    private void updatePlayerStatistics(Long matchId, BasketballStatisticsDecorator statisticsDecorator) {
        // 从数据库获取比赛中的所有得分事件
        // List<ScoreEvent> scoreEvents = scoreEventRepository.findByMatchId(matchId);
        
        // 更新统计数据
        // for (ScoreEvent event : scoreEvents) {
        //     statisticsDecorator.recordPlayerScore(event.getPlayerId(), event.getPoints());
        // }
    }
    
    /**
     * 检查比赛时间是否结束
     */
    private boolean isMatchTimeOver(BasketballMatchRecordDecorator.MatchRecord matchRecord) {
        // 实际实现中，应该检查比赛时间是否已经用完
        // 这里使用简化的实现，假设已经结束
        return true;
    }
}
