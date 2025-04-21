package com.example.event.service.impl;

import com.example.event.DTO.*;
import com.example.event.service.MatchDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 比赛数据服务实现类
 * 提供获取比赛相关数据的具体实现
 * 当前实现返回临时固定数据，后续可扩展为从数据库获取真实数据
 */
@Service
public class MatchDataServiceImpl implements MatchDataService {

    @Override
    public List<TeamSummary> getSummary(String matchId) {
        // 临时返回固定数据，后续可根据matchId从数据库获取
        return Arrays.asList(
                new TeamSummary("Grizzlies", 116, "西部第8", 2636234, 42.0),
                new TeamSummary("Warriors", 121, "西部第7", 3672283, 58.0)
        );
    }

    @Override
    public List<TeamQuarters> getQuarterScores(String matchId) {
        // 临时返回固定数据，后续可根据matchId从数据库获取
        return Arrays.asList(
                new TeamQuarters("Grizzlies", 25, 30, 36, 25),
                new TeamQuarters("Warriors", 31, 36, 27, 27)
        );
    }

    @Override
    public HighlightsData getHighlights(String matchId) {
        // 临时返回固定数据，后续可根据matchId从数据库获取
        return new HighlightsData(
                Arrays.asList(
                        new PlayerScore("贝恩", 30),
                        new PlayerScore("巴特勒", 38)
                ),
                Arrays.asList(
                        new PlayerScore("伊迪", 17),
                        new PlayerScore("库里", 37)
                ),
                Arrays.asList(
                        new PlayerScore("小皮蓬", 10),
                        new PlayerScore("格林", 10)
                )
        );
    }

    @Override
    public List<TeamStatistics> getTeamStats(String matchId) {
        // 临时返回固定数据，后续可根据matchId从数据库获取
        return Arrays.asList(
                new TeamStatistics("Grizzlies", 116, 50, 22, 48.8, 46.2),
                new TeamStatistics("Warriors", 121, 39, 29, 45.9, 34.9)
        );
    }

    @Override
    public ShotChart getShotChart(String matchId) {
        // 临时返回固定数据，后续可根据matchId从数据库获取
        return new ShotChart(
                new TeamShot("Grizzlies", 42, 86, 48.8),
                new TeamShot("Warriors", 39, 85, 45.9)
        );
    }
}