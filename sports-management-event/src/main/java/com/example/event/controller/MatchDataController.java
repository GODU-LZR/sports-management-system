package com.example.event.controller;

import com.example.event.DTO.*;
import com.example.common.response.Result;
import com.example.event.service.MatchDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/match/data/{matchId}")
public class MatchDataController {

    @Autowired
    private MatchDataService matchDataService;

    /**
     * 接口1：比赛概览
     */
    @GetMapping("/summary")
    public Result<List<TeamSummary>> getSummary(@PathVariable("matchId") String matchId) {
        return Result.success(matchDataService.getSummary(matchId));
    }

    /**
     * 接口2：分节得分
     */
    @GetMapping("/quarters")
    public Result<List<TeamQuarters>> getQuarterScores(@PathVariable("matchId") String matchId) {
        return Result.success(matchDataService.getQuarterScores(matchId));
    }

    /**
     * 接口3：球员高光
     */
    @GetMapping("/highlights")
    public Result<HighlightsData> getHighlights(@PathVariable("matchId") String matchId) {
        return Result.success(matchDataService.getHighlights(matchId));
    }

    /**
     * 接口4：全队综合统计
     */
    @GetMapping("/teamstats")
    public Result<List<TeamStatistics>> getTeamStats(@PathVariable("matchId") String matchId) {
        return Result.success(matchDataService.getTeamStats(matchId));
    }

    /**
     * 接口5：投篮热图数据（这里只返回命中/打铁的总数和百分比）
     */
    @GetMapping("/shotchart")
    public Result<ShotChart> getShotChart(@PathVariable("matchId") String matchId) {
        return Result.success(matchDataService.getShotChart(matchId));
    }
}
