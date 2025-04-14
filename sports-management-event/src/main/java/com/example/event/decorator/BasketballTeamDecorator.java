package com.example.event.decorator;

import com.example.event.entity.SportEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 篮球赛事队伍管理装饰器
 * 用于为篮球赛事添加队伍和球员管理功能
 */
@Getter
@Setter
public class BasketballTeamDecorator extends EventDecorator {
    
    /**
     * 参赛队伍信息 (队伍ID -> 队伍信息)
     */
    private Map<Long, TeamInfo> teams = new HashMap<>();
    
    /**
     * 球员信息 (球员ID -> 球员信息)
     */
    private Map<Long, PlayerInfo> players = new HashMap<>();
    
    public BasketballTeamDecorator(SportEvent decoratedEvent) {
        super(decoratedEvent);
    }
    
    /**
     * 添加参赛队伍
     * @param teamId 队伍ID
     * @param teamName 队伍名称
     * @param coach 教练名称
     * @return 是否添加成功
     */
    public boolean addTeam(Long teamId, String teamName, String coach) {
        if (teams.containsKey(teamId)) {
            return false; // 队伍已存在
        }
        
        TeamInfo teamInfo = new TeamInfo();
        teamInfo.setTeamId(teamId);
        teamInfo.setTeamName(teamName);
        teamInfo.setCoach(coach);
        
        teams.put(teamId, teamInfo);
        return true;
    }
    
    /**
     * 添加球员到队伍
     * @param playerId 球员ID
     * @param teamId 队伍ID
     * @param playerName 球员姓名
     * @param jerseyNumber 球衣号码
     * @param position 场上位置
     * @return 是否添加成功
     */
    public boolean addPlayer(Long playerId, Long teamId, String playerName, Integer jerseyNumber, String position) {
        if (!teams.containsKey(teamId) || players.containsKey(playerId)) {
            return false; // 队伍不存在或球员已存在
        }
        
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setPlayerId(playerId);
        playerInfo.setTeamId(teamId);
        playerInfo.setPlayerName(playerName);
        playerInfo.setJerseyNumber(jerseyNumber);
        playerInfo.setPosition(position);
        
        players.put(playerId, playerInfo);
        
        // 将球员添加到队伍的球员列表中
        TeamInfo teamInfo = teams.get(teamId);
        teamInfo.getPlayerIds().add(playerId);
        
        return true;
    }
    
    /**
     * 获取队伍所有球员
     * @param teamId 队伍ID
     * @return 球员信息列表
     */
    public List<PlayerInfo> getTeamPlayers(Long teamId) {
        if (!teams.containsKey(teamId)) {
            return new ArrayList<>(); // 队伍不存在
        }
        
        List<PlayerInfo> teamPlayers = new ArrayList<>();
        TeamInfo teamInfo = teams.get(teamId);
        
        for (Long playerId : teamInfo.getPlayerIds()) {
            if (players.containsKey(playerId)) {
                teamPlayers.add(players.get(playerId));
            }
        }
        
        return teamPlayers;
    }
    
    /**
     * 获取球员所属队伍
     * @param playerId 球员ID
     * @return 队伍信息
     */
    public TeamInfo getPlayerTeam(Long playerId) {
        if (!players.containsKey(playerId)) {
            return null; // 球员不存在
        }
        
        PlayerInfo playerInfo = players.get(playerId);
        return teams.get(playerInfo.getTeamId());
    }
    
    @Override
    public String getSportType() {
        return decoratedEvent.getSportType();
    }
    
    /**
     * 队伍信息内部类
     */
    @Getter
    @Setter
    public static class TeamInfo {
        private Long teamId;
        private String teamName;
        private String coach;
        private List<Long> playerIds = new ArrayList<>();
    }
    
    /**
     * 球员信息内部类
     */
    @Getter
    @Setter
    public static class PlayerInfo {
        private Long playerId;
        private Long teamId;
        private String playerName;
        private Integer jerseyNumber;
        private String position; // PG, SG, SF, PF, C (控球后卫, 得分后卫, 小前锋, 大前锋, 中锋)
    }
}