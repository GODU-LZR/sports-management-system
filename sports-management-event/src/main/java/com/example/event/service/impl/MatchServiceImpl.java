package com.example.event.service.impl;

import com.example.event.service.MatchService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 比赛服务实现类
 */
@Service
public class MatchServiceImpl implements MatchService {

    @Override
    public boolean existsById() {
        // 实现检查比赛是否存在的逻辑
        return true;
    }

    @Override
    public Map<String, Object> getMatchData(String matchId, Long userId) {
        // 实现获取比赛基本信息的逻辑
        // 需要验证该比赛所属的赛事是否是该用户创建的
        Map<String, Object> matchData = new HashMap<>();
        matchData.put("matchId", matchId);
        matchData.put("sport", "篮球");
        matchData.put("awayTeamId", "123");
        matchData.put("homeTeamId", "456");
        matchData.put("awayTeam", "软件1223");
        matchData.put("homeTeam", "软件1224");
        matchData.put("venueId", "4");
        matchData.put("venue", "篮球场3号");
        matchData.put("start_time", "2025-01-03 14:00");
        matchData.put("end_time", "2025-01-03 15:30");
        matchData.put("responsiblePersonId", "4");
        matchData.put("responsiblePerson", "张管理员");
        matchData.put("phone", "13800138000");
        matchData.put("note", "请各队提前30分钟到场热身，迟到15分钟视为弃权");
        matchData.put("phase", 1);
        matchData.put("winner", "软件1224");
        
        List<Map<String, Object>> referees = new ArrayList<>();
        Map<String, Object> referee1 = new HashMap<>();
        referee1.put("refereeId", "1");
        referee1.put("name", "张教练");
        referees.add(referee1);
        
        Map<String, Object> referee2 = new HashMap<>();
        referee2.put("refereeId", "2");
        referee2.put("name", "王教练");
        referees.add(referee2);
        
        matchData.put("referee", referees);
        
        return matchData;
    }

    @Override
    public List<Map<String, Object>> getQuartersData(String matchId, Long userId) {
        // 实现获取赛段得分信息的逻辑
        // 可以复用MatchDataService中的相关方法
        List<Map<String, Object>> quartersData = new ArrayList<>();
        
        Map<String, Object> awayTeam = new HashMap<>();
        awayTeam.put("team", "Grizzlies");
        awayTeam.put("one", 25);
        awayTeam.put("two", 30);
        awayTeam.put("three", 36);
        awayTeam.put("four", 25);
        quartersData.add(awayTeam);
        
        Map<String, Object> homeTeam = new HashMap<>();
        homeTeam.put("team", "Warriors");
        homeTeam.put("one", 31);
        homeTeam.put("two", 36);
        homeTeam.put("three", 27);
        homeTeam.put("four", 27);
        quartersData.add(homeTeam);
        
        return quartersData;
    }

    @Override
    public List<Map<String, Object>> getTeamStatsData(String matchId, Long userId) {
        // 实现获取队伍得分信息的逻辑
        // 可以复用MatchDataService中的相关方法
        List<Map<String, Object>> teamStatsData = new ArrayList<>();
        
        Map<String, Object> awayTeam = new HashMap<>();
        awayTeam.put("name", "Grizzlies");
        awayTeam.put("pts", 116);
        awayTeam.put("reb", 50);
        awayTeam.put("ast", 39);
        awayTeam.put("fgPct", 48.8);
        awayTeam.put("tpPct", 46.2);
        teamStatsData.add(awayTeam);
        
        Map<String, Object> homeTeam = new HashMap<>();
        homeTeam.put("name", "Warriors");
        homeTeam.put("pts", 121);
        homeTeam.put("reb", 39);
        homeTeam.put("ast", 29);
        homeTeam.put("fgPct", 45.9);
        homeTeam.put("tpPct", 34.9);
        teamStatsData.add(homeTeam);
        
        return teamStatsData;
    }

    @Override
    public List<List<Map<String, Object>>> getPlayersData(String matchId, Long userId) {
        // 实现获取球员得分信息的逻辑
        // 可以复用MatchDataService中的相关方法
        return getSportMatchPlayersData(matchId);
    }

    @Override
    public boolean updateMatchData(String matchId, Map<String, Object> form, Long userId) {
        // 实现修改比赛基本信息的逻辑
        // 需要验证该比赛所属的赛事是否是该用户创建的
        return true;
    }

    @Override
    public boolean updateQuartersData(String matchId, List<Map<String, Object>> form, Long userId) {
        // 实现修改赛段得分信息的逻辑
        // 需要验证该比赛所属的赛事是否是该用户创建的
        return true;
    }

    @Override
    public boolean updateTeamStatsData(String matchId, List<Map<String, Object>> form, Long userId) {
        // 实现修改队伍得分信息的逻辑
        // 需要验证该比赛所属的赛事是否是该用户创建的
        return true;
    }

    @Override
    public boolean updatePlayersData(String matchId, Map<String, Object> form, Long userId) {
        // 实现修改球员得分信息的逻辑
        // 需要验证该比赛所属的赛事是否是该用户创建的
        return true;
    }

    @Override
    public boolean toggleFollowStatus(String matchId, Long userId) {
        // 实现切换比赛关注状态的逻辑
        return true;
    }

    @Override
    public List<List<Map<String, Object>>> getSportMatchPlayersData(String matchId) {
        // 实现获取比赛球员得分信息的逻辑
        List<List<Map<String, Object>>> playersData = new ArrayList<>();
        
        // 客队球员数据
        List<Map<String, Object>> awayTeamPlayers = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> player = new HashMap<>();
            player.put("name", "莫兰特");
            player.put("minutes", "34:54");
            player.put("pts", 22);
            player.put("reb", 3);
            player.put("ast", 3);
            player.put("fgMade", 9);
            player.put("fgAtt", 18);
            awayTeamPlayers.add(player);
        }
        playersData.add(awayTeamPlayers);
        
        // 主队球员数据
        List<Map<String, Object>> homeTeamPlayers = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> player = new HashMap<>();
            player.put("name", "莫兰特");
            player.put("minutes", "34:54");
            player.put("pts", 22);
            player.put("reb", 3);
            player.put("ast", 3);
            player.put("fgMade", 9);
            player.put("fgAtt", 18);
            homeTeamPlayers.add(player);
        }
        playersData.add(homeTeamPlayers);
        
        return playersData;
    }
}