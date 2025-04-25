package com.example.event.controller;

import com.example.event.DTO.*;
import com.example.common.response.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    /**
     * 接口1：比赛概览
     */
    @GetMapping("/summary")
    public Result<List<TeamSummary>> getSummary() {
        List<TeamSummary> data = Arrays.asList(
                new TeamSummary("Grizzlies", 116, "西部第8", 2636234, 42.0),
                new TeamSummary("Warriors", 121, "西部第7", 3672283, 58.0)
        );
        return Result.success(data);
    }

    /**
     * 接口2：分节得分
     */
    @GetMapping("/quarters")
    public Result<List<TeamQuarters>> getQuarterScores() {
        List<TeamQuarters> data = Arrays.asList(
                new TeamQuarters("Grizzlies", 25, 30, 36, 25),
                new TeamQuarters("Warriors", 31, 36, 27, 27)
        );
        return Result.success(data);
    }

    /**
     * 接口3：球员高光
     */
//    @GetMapping("/highlights")
//    public Result<HighlightsData> getHighlights() {
//        HighlightsData data = new HighlightsData(
//                Arrays.asList(
//                        new PlayerScore("贝恩", 30, player.getTeamId()),
//                        new PlayerScore("巴特勒", 38, player.getTeamId())
//                ),
//                Arrays.asList(
//                        new PlayerScore("伊迪", 17, player.getTeamId()),
//                        new PlayerScore("库里", 37, player.getTeamId())
//                ),
//                Arrays.asList(
//                        new PlayerScore("小皮蓬", 10, player.getTeamId()),
//                        new PlayerScore("格林", 10, player.getTeamId())
//                )
//        );
//        return Result.success(data);
//    }

    /**
     * 接口4：全队综合统计
     */
    @GetMapping("/team-stats")
    public Result<List<TeamStatistics>> getTeamStats() {
        List<TeamStatistics> data = Arrays.asList(
                new TeamStatistics("Grizzlies", 116, 50, 22, 48.8, 46.2),
                new TeamStatistics("Warriors", 121, 39, 29, 45.9, 34.9)
        );
        return Result.success(data);
    }

    /**
     * 接口5：投篮热图数据（这里只返回命中/打铁的总数和百分比）
     */
    @GetMapping("/shot-chart")
    public Result<ShotChart> getShotChart() {
        ShotChart data = new ShotChart(
                new ShotChart.TeamShot("Grizzlies", 42, 86, 48.8),
                new ShotChart.TeamShot("Warriors",  39, 85, 45.9)
        );
        return Result.success(data);
    }

    // ===== 内部静态类定义所有返回结构 =====

}
