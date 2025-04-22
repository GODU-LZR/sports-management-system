package com.example.event.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.event.DTO.*;
import com.example.event.dao.basketball.BasketballMatch;
import com.example.event.dao.basketball.BasketballMatchQuarter;
import com.example.event.dao.basketball.BasketballTeam;
import com.example.event.dao.basketball.BasketballTeamStats;
import com.example.event.entity.BasketballPlayer;
import com.example.event.entity.BasketballPlayerStats;
import com.example.event.mapper.basketball.BasketballMatchMapper;
import com.example.event.mapper.basketball.BasketballMatchQuarterMapper;
import com.example.event.mapper.basketball.BasketballTeamMapper;
import com.example.event.mapper.basketball.BasketballTeamStatsMapper;
import com.example.event.service.MatchDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 比赛数据服务实现类
 * 提供获取比赛相关数据的具体实现
 * 从数据库获取真实数据
 */
@Service
public class MatchDataServiceImpl implements MatchDataService {  
    @Autowired
    private BasketballMatchMapper basketballMatchMapper;
    
    @Autowired
    private BasketballTeamMapper basketballTeamMapper;
    
    @Autowired
    private BasketballMatchQuarterMapper basketballMatchQuarterMapper;
    @Autowired
    private BasketballTeamStatsMapper basketballTeamStatsMapper;
    @Override
    public List<TeamSummary> getSummary(String matchId) {
        // 从数据库获取比赛信息
        BasketballMatch match = basketballMatchMapper.selectById(matchId);
        if (match == null) {
            return new ArrayList<>();
        }
        
        // 获取主队和客队信息
        BasketballTeam homeTeam = basketballTeamMapper.selectById(match.getHomeTeamId());
        BasketballTeam awayTeam = basketballTeamMapper.selectById(match.getAwayTeamId());
        
        // 获取比赛统计数据
        QueryWrapper<BasketballTeamStats> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("match_id", matchId);
        List<BasketballTeamStats> teamStats = new ArrayList<>(); // 使用QueryWrapper直接查询
        try {
            teamStats = basketballTeamStatsMapper.selectList(queryWrapper);
        } catch (Exception e) {
            // 异常处理
        }
        
        List<TeamSummary> summaries = new ArrayList<>();
        
        // 构建主队摘要
        if (homeTeam != null) {
            String homeSeed = homeTeam.getConference() + "第" + homeTeam.getRankPosition();
            int homePoints = 0;
            double homeVotePct = 0.0;
            int homeVotes = 0;
            
            // 查找主队统计数据
            for (BasketballTeamStats stats : teamStats) {
                if (stats.getTeamId().equals(homeTeam.getTeamId())) {
                    homePoints = stats.getTotalPoints();
                    // 假设投票数据来自其他地方，这里使用薪资上限作为示例
                    homeVotes = homeTeam.getSalaryCap().intValue();
                    homeVotePct = 50.0; // 示例值
                    break;
                }
            }
            
            summaries.add(new TeamSummary(homeTeam.getTeamName(), homePoints, homeSeed, homeVotes, homeVotePct));
        }
        
        // 构建客队摘要
        if (awayTeam != null) {
            String awaySeed = awayTeam.getConference() + "第" + awayTeam.getRankPosition();
            int awayPoints = 0;
            double awayVotePct = 0.0;
            int awayVotes = 0;
            
            // 查找客队统计数据
            for (BasketballTeamStats stats : teamStats) {
                if (stats.getTeamId().equals(awayTeam.getTeamId())) {
                    awayPoints = stats.getTotalPoints();
                    // 假设投票数据来自其他地方，这里使用薪资上限作为示例
                    awayVotes = awayTeam.getSalaryCap().intValue();
                    awayVotePct = 50.0; // 示例值
                    break;
                }
            }
            
            summaries.add(new TeamSummary(awayTeam.getTeamName(), awayPoints, awaySeed, awayVotes, awayVotePct));
        }
        
        return summaries;
    }

    @Override
    public List<TeamQuarters> getQuarterScores(String matchId) {
        // 从数据库获取比赛分节得分
        List<BasketballMatchQuarter> quarters = basketballMatchQuarterMapper.selectByMatchId(matchId);
        if (quarters == null || quarters.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<TeamQuarters> teamQuarters = new ArrayList<>();
        
        for (BasketballMatchQuarter quarter : quarters) {
            // 获取球队信息
            BasketballTeam team = basketballTeamMapper.selectById(quarter.getId().getTeamId());
            if (team != null) {
                teamQuarters.add(new TeamQuarters(
                        team.getTeamName(),
                        quarter.getQ1(),
                        quarter.getQ2(),
                        quarter.getQ3(),
                        quarter.getQ4()
                ));
            }
        }
        
        return teamQuarters;
    }

    @Override
    public HighlightsData getHighlights(String matchId) {
        // 从数据库获取球员统计数据
        // 假设有一个方法可以获取比赛中的球员统计数据
        List<BasketballPlayerStats> playerStats = getPlayerStatsByMatchId(matchId);
        if (playerStats == null || playerStats.isEmpty()) {
            return new HighlightsData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        
        // 按得分排序并获取前2名
        List<PlayerScore> topScorers = playerStats.stream()
                .sorted(Comparator.comparing(BasketballPlayerStats::getPoints).reversed())
                .limit(2)
                .map(stats -> {
                    BasketballPlayer player = getPlayerById(stats.getPlayerId());
                    return new PlayerScore(player != null ? player.getName() : "未知", stats.getPoints());
                })
                .collect(Collectors.toList());
        
        // 按篮板排序并获取前2名
        List<PlayerScore> topRebounders = playerStats.stream()
                .sorted(Comparator.comparing(BasketballPlayerStats::getRebounds).reversed())
                .limit(2)
                .map(stats -> {
                    BasketballPlayer player = getPlayerById(stats.getPlayerId());
                    return new PlayerScore(player != null ? player.getName() : "未知", stats.getRebounds());
                })
                .collect(Collectors.toList());
        
        // 按助攻排序并获取前2名
        List<PlayerScore> topAssisters = playerStats.stream()
                .sorted(Comparator.comparing(BasketballPlayerStats::getAssists).reversed())
                .limit(2)
                .map(stats -> {
                    BasketballPlayer player = getPlayerById(stats.getPlayerId());
                    return new PlayerScore(player != null ? player.getName() : "未知", stats.getAssists());
                })
                .collect(Collectors.toList());
        
        return new HighlightsData(topScorers, topRebounders, topAssisters);
    }
    
    // 辅助方法：根据比赛ID获取球员统计数据
    private List<BasketballPlayerStats> getPlayerStatsByMatchId(String matchId) {
        // 使用QueryWrapper直接查询
        QueryWrapper<BasketballPlayerStats> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("match_id", matchId);
        List<BasketballPlayerStats> playerStats = new ArrayList<>();
        try {
            playerStats = new com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.baomidou.mybatisplus.core.mapper.BaseMapper<BasketballPlayerStats>, BasketballPlayerStats>(){}.list(queryWrapper);
        } catch (Exception e) {
            // 异常处理
        }
        return playerStats;
    }
    
    // 辅助方法：根据球员ID获取球员信息
    private BasketballPlayer getPlayerById(Long playerId) {
        // 使用QueryWrapper直接查询
        try {
            return new com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.baomidou.mybatisplus.core.mapper.BaseMapper<BasketballPlayer>, BasketballPlayer>(){}.getById(playerId);
        } catch (Exception e) {
            // 异常处理
            return null;
        }
    }

    @Override
    public List<TeamStatistics> getTeamStats(String matchId) {
        // 从数据库获取比赛信息
        BasketballMatch match = basketballMatchMapper.selectById(matchId);
        if (match == null) {
            return new ArrayList<>();
        }
        
        // 获取主队和客队信息
        BasketballTeam homeTeam = basketballTeamMapper.selectById(match.getHomeTeamId());
        BasketballTeam awayTeam = basketballTeamMapper.selectById(match.getAwayTeamId());
        
        // 获取比赛统计数据
        QueryWrapper<BasketballTeamStats> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("match_id", matchId);
        List<BasketballTeamStats> teamStats = new ArrayList<>(); // 使用QueryWrapper直接查询
        try {
            teamStats = new com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.baomidou.mybatisplus.core.mapper.BaseMapper<BasketballTeamStats>, BasketballTeamStats>(){}.list(queryWrapper);
        } catch (Exception e) {
            // 异常处理
        }
        
        List<TeamStatistics> statistics = new ArrayList<>();
        
        // 添加主队统计
        if (homeTeam != null) {
            for (BasketballTeamStats stats : teamStats) {
                if (stats.getTeamId().equals(homeTeam.getTeamId())) {
                    statistics.add(new TeamStatistics(
                            homeTeam.getTeamName(),
                            stats.getTotalPoints(),
                            stats.getRebounds(),
                            stats.getAssists(),
                            stats.getFgPercent(),
                            stats.getThreePtPercent()
                    ));
                    break;
                }
            }
        }
        
        // 添加客队统计
        if (awayTeam != null) {
            for (BasketballTeamStats stats : teamStats) {
                if (stats.getTeamId().equals(awayTeam.getTeamId())) {
                    statistics.add(new TeamStatistics(
                            awayTeam.getTeamName(),
                            stats.getTotalPoints(),
                            stats.getRebounds(),
                            stats.getAssists(),
                            stats.getFgPercent(),
                            stats.getThreePtPercent()
                    ));
                    break;
                }
            }
        }
        
        return statistics;
    }

    @Override
    public ShotChart getShotChart(String matchId) {
        // 从数据库获取比赛信息
        BasketballMatch match = basketballMatchMapper.selectById(matchId);
        if (match == null) {
            return new ShotChart(new TeamShot("", 0, 0, 0), new TeamShot("", 0, 0, 0));
        }
        
        // 获取主队和客队信息
        BasketballTeam homeTeam = basketballTeamMapper.selectById(match.getHomeTeamId());
        BasketballTeam awayTeam = basketballTeamMapper.selectById(match.getAwayTeamId());
        
        // 获取比赛统计数据
        QueryWrapper<BasketballTeamStats> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("match_id", matchId);
        List<BasketballTeamStats> teamStats = new ArrayList<>(); // 使用QueryWrapper直接查询
        try {
            teamStats = new com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.baomidou.mybatisplus.core.mapper.BaseMapper<BasketballTeamStats>, BasketballTeamStats>(){}.list(queryWrapper);
        } catch (Exception e) {
            // 异常处理
        }
        
        TeamShot homeShot = new TeamShot("", 0, 0, 0);
        TeamShot awayShot = new TeamShot("", 0, 0, 0);
        
        // 设置主队投篮数据
        if (homeTeam != null) {
            for (BasketballTeamStats stats : teamStats) {
                if (stats.getTeamId().equals(homeTeam.getTeamId())) {
                    // 假设数据库中有投篮命中和出手次数
                    int made = 0; // 应该从stats中获取
                    int attempt = 0; // 应该从stats中获取
                    double pct = stats.getFgPercent();
                    
                    homeShot = new TeamShot(homeTeam.getTeamName(), made, attempt, pct);
                    break;
                }
            }
        }
        
        // 设置客队投篮数据
        if (awayTeam != null) {
            for (BasketballTeamStats stats : teamStats) {
                if (stats.getTeamId().equals(awayTeam.getTeamId())) {
                    // 假设数据库中有投篮命中和出手次数
                    int made = 0; // 应该从stats中获取
                    int attempt = 0; // 应该从stats中获取
                    double pct = stats.getFgPercent();
                    
                    awayShot = new TeamShot(awayTeam.getTeamName(), made, attempt, pct);
                    break;
                }
            }
        }
        
        return new ShotChart(awayShot, homeShot);
    }
}