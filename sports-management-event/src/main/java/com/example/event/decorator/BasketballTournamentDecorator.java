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
 * 篮球赛事比赛安排装饰器
 * 用于为篮球赛事添加比赛安排和赛程管理功能
 */
@Getter
@Setter
public class BasketballTournamentDecorator extends EventDecorator {
    
    /**
     * 比赛阶段（如：小组赛、淘汰赛、决赛等）
     */
    private String tournamentStage;
    
    /**
     * 比赛日程安排 (比赛ID -> 比赛时间)
     */
    private Map<Long, LocalDateTime> matchSchedule = new HashMap<>();
    
    /**
     * 小组分组信息 (小组ID -> 队伍ID列表)
     */
    private Map<String, List<Long>> groupTeams = new HashMap<>();
    
    /**
     * 淘汰赛对阵信息
     */
    private List<MatchPair> knockoutMatches = new ArrayList<>();
    
    public BasketballTournamentDecorator(SportEvent decoratedEvent) {
        super(decoratedEvent);
    }
    
    /**
     * 安排小组赛
     * @param groupCount 小组数量
     * @param teamIds 所有参赛队伍ID
     * @return 是否安排成功
     */
    public boolean arrangeGroupStage(int groupCount, List<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return false;
        }
        
        // 设置当前阶段为小组赛
        this.tournamentStage = "GROUP_STAGE";
        
        // 简单的分组逻辑：平均分配队伍到各小组
        int teamsPerGroup = teamIds.size() / groupCount;
        int remainingTeams = teamIds.size() % groupCount;
        
        int teamIndex = 0;
        for (int i = 0; i < groupCount; i++) {
            String groupId = "Group" + (char)('A' + i); // Group A, Group B, ...
            List<Long> groupTeamList = new ArrayList<>();
            
            // 计算当前小组应分配的队伍数
            int currentGroupTeamCount = teamsPerGroup + (i < remainingTeams ? 1 : 0);
            
            // 分配队伍到当前小组
            for (int j = 0; j < currentGroupTeamCount && teamIndex < teamIds.size(); j++) {
                groupTeamList.add(teamIds.get(teamIndex++));
            }
            
            groupTeams.put(groupId, groupTeamList);
        }
        
        return true;
    }
    
    /**
     * 安排淘汰赛
     * @param qualifiedTeams 晋级队伍ID列表
     * @return 是否安排成功
     */
    public boolean arrangeKnockoutStage(List<Long> qualifiedTeams) {
        if (qualifiedTeams == null || qualifiedTeams.isEmpty() || qualifiedTeams.size() % 2 != 0) {
            return false; // 队伍数量必须是偶数
        }
        
        // 设置当前阶段为淘汰赛
        this.tournamentStage = "KNOCKOUT_STAGE";
        
        // 清空之前的对阵信息
        knockoutMatches.clear();
        
        // 简单的对阵安排：按顺序两两配对
        for (int i = 0; i < qualifiedTeams.size(); i += 2) {
            MatchPair pair = new MatchPair();
            pair.setTeamA(qualifiedTeams.get(i));
            pair.setTeamB(qualifiedTeams.get(i + 1));
            knockoutMatches.add(pair);
        }
        
        return true;
    }
    
    /**
     * 安排决赛
     * @param finalTeamA 决赛队伍A
     * @param finalTeamB 决赛队伍B
     * @param finalTime 决赛时间
     * @return 是否安排成功
     */
    public boolean arrangeFinal(Long finalTeamA, Long finalTeamB, LocalDateTime finalTime) {
        // 设置当前阶段为决赛
        this.tournamentStage = "FINAL";
        
        // 创建决赛对阵
        MatchPair finalMatch = new MatchPair();
        finalMatch.setTeamA(finalTeamA);
        finalMatch.setTeamB(finalTeamB);
        finalMatch.setMatchTime(finalTime);
        
        // 清空之前的对阵信息并添加决赛
        knockoutMatches.clear();
        knockoutMatches.add(finalMatch);
        
        return true;
    }
    
    /**
     * 安排比赛时间
     * @param matchId 比赛ID
     * @param matchTime 比赛时间
     */
    public void scheduleMatch(Long matchId, LocalDateTime matchTime) {
        matchSchedule.put(matchId, matchTime);
    }
    
    /**
     * 获取比赛时间
     * @param matchId 比赛ID
     * @return 比赛时间
     */
    public LocalDateTime getMatchTime(Long matchId) {
        return matchSchedule.get(matchId);
    }
    
    @Override
    public String getSportType() {
        return decoratedEvent.getSportType();
    }
    
    /**
     * 比赛对阵信息内部类
     */
    @Getter
    @Setter
    public static class MatchPair {
        private Long teamA;
        private Long teamB;
        private Long winnerId;
        private LocalDateTime matchTime;
    }
}