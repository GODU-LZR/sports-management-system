package com.example.event.decorator;

import com.example.event.entity.SportEvent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 篮球赛事比赛记录装饰器
 * 用于为篮球赛事添加比赛记录和比分管理功能
 */
@Getter
@Setter
public class BasketballMatchRecordDecorator extends EventDecorator {
    
    /**
     * 比赛记录 (比赛ID -> 比赛记录)
     */
    private Map<Long, MatchRecord> matchRecords = new HashMap<>();
    
    public BasketballMatchRecordDecorator(SportEvent decoratedEvent) {
        super(decoratedEvent);
    }
    
    /**
     * 创建新比赛记录
     * @param matchId 比赛ID
     * @param teamAId 队伍A ID
     * @param teamBId 队伍B ID
     * @param matchTime 比赛时间
     * @return 是否创建成功
     */
    public boolean createMatchRecord(Long matchId, Long teamAId, Long teamBId, LocalDateTime matchTime) {
        if (matchRecords.containsKey(matchId)) {
            return false; // 比赛记录已存在
        }
        
        MatchRecord record = new MatchRecord();
        record.setMatchId(matchId);
        record.setTeamAId(teamAId);
        record.setTeamBId(teamBId);
        record.setMatchTime(matchTime);
        record.setStatus("SCHEDULED"); // 初始状态为已安排
        
        // 初始化比分
        for (int i = 0; i < 4; i++) { // 常规四节
            record.getQuarterScores().add(new QuarterScore(0, 0));
        }
        
        matchRecords.put(matchId, record);
        return true;
    }
    
    /**
     * 开始比赛
     * @param matchId 比赛ID
     * @return 是否开始成功
     */
    public boolean startMatch(Long matchId) {
        if (!matchRecords.containsKey(matchId)) {
            return false; // 比赛记录不存在
        }
        
        MatchRecord record = matchRecords.get(matchId);
        record.setStatus("IN_PROGRESS");
        record.setStartTime(LocalDateTime.now());
        return true;
    }
    
    /**
     * 结束比赛
     * @param matchId 比赛ID
     * @return 是否结束成功
     */
    public boolean endMatch(Long matchId) {
        if (!matchRecords.containsKey(matchId)) {
            return false; // 比赛记录不存在
        }
        
        MatchRecord record = matchRecords.get(matchId);
        record.setStatus("COMPLETED");
        record.setEndTime(LocalDateTime.now());
        
        // 计算总比分
        int teamATotal = 0;
        int teamBTotal = 0;
        for (QuarterScore qs : record.getQuarterScores()) {
            teamATotal += qs.getTeamAScore();
            teamBTotal += qs.getTeamBScore();
        }
        
        // 加上加时赛比分
        for (QuarterScore ot : record.getOvertimeScores()) {
            teamATotal += ot.getTeamAScore();
            teamBTotal += ot.getTeamBScore();
        }
        
        record.setTeamAFinalScore(teamATotal);
        record.setTeamBFinalScore(teamBTotal);
        
        // 设置获胜队伍
        if (teamATotal > teamBTotal) {
            record.setWinnerTeamId(record.getTeamAId());
        } else if (teamBTotal > teamATotal) {
            record.setWinnerTeamId(record.getTeamBId());
        }
        
        return true;
    }
    
    /**
     * 记录单节比分
     * @param matchId 比赛ID
     * @param quarter 节数 (0-3 表示第1-4节，>=4表示加时赛)
     * @param teamAScore 队伍A得分
     * @param teamBScore 队伍B得分
     * @return 是否记录成功
     */
    public boolean recordQuarterScore(Long matchId, int quarter, int teamAScore, int teamBScore) {
        if (!matchRecords.containsKey(matchId)) {
            return false; // 比赛记录不存在
        }
        
        MatchRecord record = matchRecords.get(matchId);
        
        if (quarter < 0) {
            return false; // 无效节数
        }
        
        if (quarter < 4) { // 常规四节
            if (quarter >= record.getQuarterScores().size()) {
                return false; // 节数超出范围
            }
            
            QuarterScore qs = record.getQuarterScores().get(quarter);
            qs.setTeamAScore(teamAScore);
            qs.setTeamBScore(teamBScore);
        } else { // 加时赛
            int otIndex = quarter - 4;
            
            // 确保加时赛列表有足够的元素
            while (record.getOvertimeScores().size() <= otIndex) {
                record.getOvertimeScores().add(new QuarterScore(0, 0));
            }
            
            QuarterScore ot = record.getOvertimeScores().get(otIndex);
            ot.setTeamAScore(teamAScore);
            ot.setTeamBScore(teamBScore);
        }
        
        return true;
    }
    
    /**
     * 获取比赛记录
     * @param matchId 比赛ID
     * @return 比赛记录
     */
    public MatchRecord getMatchRecord(Long matchId) {
        return matchRecords.get(matchId);
    }
    
    /**
     * 获取队伍的所有比赛记录
     * @param teamId 队伍ID
     * @return 比赛记录列表
     */
    public List<MatchRecord> getTeamMatchRecords(Long teamId) {
        List<MatchRecord> teamMatches = new ArrayList<>();
        
        for (MatchRecord record : matchRecords.values()) {
            if (record.getTeamAId().equals(teamId) || record.getTeamBId().equals(teamId)) {
                teamMatches.add(record);
            }
        }
        
        return teamMatches;
    }
    
    @Override
    public String getSportType() {
        return decoratedEvent.getSportType();
    }
    
    /**
     * 比赛记录内部类
     */
    @Getter
    @Setter
    public static class MatchRecord {
        private Long matchId;
        private Long teamAId;
        private Long teamBId;
        private LocalDateTime matchTime;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
        private List<QuarterScore> quarterScores = new ArrayList<>(); // 常规四节比分
        private List<QuarterScore> overtimeScores = new ArrayList<>(); // 加时赛比分
        private Integer teamAFinalScore;
        private Integer teamBFinalScore;
        private Long winnerTeamId;
    }
    
    /**
     * 单节比分内部类
     */
    @Getter
    @Setter
    public static class QuarterScore {
        private Integer teamAScore;
        private Integer teamBScore;
        
        public QuarterScore(Integer teamAScore, Integer teamBScore) {
            this.teamAScore = teamAScore;
            this.teamBScore = teamBScore;
        }
    }
}