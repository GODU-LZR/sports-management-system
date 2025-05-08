package com.example.event.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.event.DTO.basketball.matchdata.*;
import com.example.event.dao.basketball.*;
import com.example.event.DTO.basketball.BasketballPlayer;
import com.example.event.mapper.basketball.*;
import com.example.event.service.MatchDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
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
    @Autowired
    private BasketballMatchPlayerDetailMapper basketballMatchPlayerDetailMapper;
    @Autowired
    private BasketballShotChartMapper basketballShotChartMapper;

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
        HighlightsData highlights = new HighlightsData();
        List<BasketballMatchPlayerDetails>[] playerDetailsList = getPlayerStatsByMatchId(matchId);

        if (playerDetailsList != null) {
            for (List<BasketballMatchPlayerDetails> playerDetails : playerDetailsList) {
                // 找出得分最高的球员
                Optional<BasketballMatchPlayerDetails> topScorer = playerDetails.stream()
                        .max(Comparator.comparingInt(BasketballMatchPlayerDetails::getPoints));
                topScorer.ifPresent(player -> highlights.pt.add(new PlayerScore(player.getName(), player.getPoints(), player.getTeamId())));

                // 找出篮板最高的球员
                Optional<BasketballMatchPlayerDetails> topRebounder = playerDetails.stream()
                        .max(Comparator.comparingInt(BasketballMatchPlayerDetails::getRebounds));
                topRebounder.ifPresent(player -> highlights.reb.add(new PlayerScore(player.getName(), player.getRebounds(), player.getTeamId())));

                // 找出助攻最高的球员
                Optional<BasketballMatchPlayerDetails> topAssister = playerDetails.stream()
                        .max(Comparator.comparingInt(BasketballMatchPlayerDetails::getAssists));
                topAssister.ifPresent(player -> highlights.ast.add(new PlayerScore(player.getName(), player.getAssists(), player.getTeamId())));
            }
        }
        return highlights;
    }

    // 辅助方法：根据比赛ID获取球员统计数据
    private List<BasketballMatchPlayerDetails>[] getPlayerStatsByMatchId(String matchId) {
        // 使用QueryWrapper直接查询
        QueryWrapper<BasketballMatchPlayerDetails> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("match_id", matchId);

        try {
            List<BasketballMatchPlayerDetails> playerStatsList = basketballMatchPlayerDetailMapper.selectList(queryWrapper);
            if (playerStatsList != null) {

//            知道具体id的写法
//            Map<Integer,List<BasketballMatchPlayerDetails>>integerListMap=playerStatsList.stream().collect(Collectors.groupingBy(BasketballMatchPlayerDetails::getTeamId));
                Predicate<BasketballMatchPlayerDetails> isTeamOne = plater -> plater.getTeamId().equals(playerStatsList.get(1).getTeamId());
                Map<Boolean, List<BasketballMatchPlayerDetails>> partitionedPlayers = playerStatsList.stream().collect(Collectors.partitioningBy(isTeamOne));
                List<BasketballMatchPlayerDetails> teamOne = partitionedPlayers.get(true);
                List<BasketballMatchPlayerDetails> teamTow = partitionedPlayers.get(false);
                return new List[]{teamOne, teamTow};

            }
        } catch (Exception e) {
            // 异常处理
        }
        return null;
    }

    // 辅助方法：根据球员ID获取球员信息
    private BasketballPlayer getPlayerById(Long playerId) {
        // 使用QueryWrapper直接查询
        try {
            return new com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.baomidou.mybatisplus.core.mapper.BaseMapper<BasketballPlayer>, BasketballPlayer>() {
            }.getById(playerId);
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

        List<BasketballTeamStats> teamStatsList = basketballTeamStatsMapper.selectList(queryWrapper);

        // 将 teamStatsList 转换为以 teamId 为 key 的 Map，方便查找，为后续超多队伍进行铺垫
        Map<Long, BasketballTeamStats> teamStatsMap = teamStatsList.stream()
                .collect(Collectors.toMap(BasketballTeamStats::getTeamId, Function.identity()));

        List<TeamStatistics> statistics = new ArrayList<>();

        // 添加主队统计
        if (homeTeam != null && teamStatsMap.containsKey(homeTeam.getTeamId())) {
            BasketballTeamStats stats = teamStatsMap.get(homeTeam.getTeamId());
            statistics.add(new TeamStatistics(
                    homeTeam.getTeamName(),
                    stats.getTotalPoints(),
                    stats.getRebounds(),
                    stats.getAssists(),
                    stats.getFgPercent(),
                    stats.getThreePtPercent()
            ));
        }

        // 添加客队统计
        if (awayTeam != null && teamStatsMap.containsKey(awayTeam.getTeamId())) {
            BasketballTeamStats stats = teamStatsMap.get(awayTeam.getTeamId());
            statistics.add(new TeamStatistics(
                    awayTeam.getTeamName(),
                    stats.getTotalPoints(),
                    stats.getRebounds(),
                    stats.getAssists(),
                    stats.getFgPercent(),
                    stats.getThreePtPercent()
            ));
        }

        return statistics;
    }

    @Override
    public ShotChart getShotChart(String matchId) {


        ShotChart teamStats = basketballShotChartMapper.selectShotChart(matchId);
        teamStats.setMatchId(matchId);
        System.out.println(teamStats.getMatchId());

        if (teamStats == null) {
            throw new RuntimeException();
        }
        return teamStats;
    }
}