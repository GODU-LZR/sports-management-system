package com.example.event.controller;

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
    public MatchSummary getSummary() {
        return new MatchSummary(
                "Grizzlies", // 客队英文名
                "Warriors",  // 主队英文名
                116,
                121,
                "2025-04-16 10:00",
                "西部第8",
                "西部第7"
        );
    }

    /**
     * 接口2：分节得分
     */
    @GetMapping("/quarters")
    public List<QuarterScore> getQuarterScores() {
        return Arrays.asList(
                new QuarterScore(1, 25, 31),
                new QuarterScore(2, 30, 36),
                new QuarterScore(3, 36, 27),
                new QuarterScore(4, 25, 27)
        );
    }

    /**
     * 接口3：球员高光
     */
    @GetMapping("/highlights")
    public List<PlayerHighlight> getHighlights() {
        return Arrays.asList(
                new PlayerHighlight("贝恩",    30, 6, 4),
                new PlayerHighlight("伊迪",    17, 17, 2),
                new PlayerHighlight("小皮蓬",   5, 5, 10),
                new PlayerHighlight("巴特勒",   38, 7, 6),
                new PlayerHighlight("库里",    37, 8, 4),
                new PlayerHighlight("格林",     4, 6, 10)
        );
    }

    /**
     * 接口4：全队综合统计
     */
    @GetMapping("/team-stats")
    public TeamStats getTeamStats() {
        return new TeamStats(
                116, 121,
                50,  39,
                22,  29,
                48.8, 45.9,
                46.2, 34.9
        );
    }

    /**
     * 接口5：投篮热图数据（这里只返回命中/打铁的总数和百分比）
     */
    @GetMapping("/shot-chart")
    public ShotChart getShotChart() {
        return new ShotChart(
                new TeamShot("Grizzlies", 42, 86, 48.8),
                new TeamShot("Warriors",  39, 85, 45.9)
        );
    }

    // ===== 内部静态类定义所有返回结构 =====

    /** 比赛概览 */
    public static class MatchSummary {
        public String awayTeam;
        public String homeTeam;
        public int awayScore;
        public int homeScore;
        public String dateTime;
        public String awaySeed;
        public String homeSeed;

        public MatchSummary(String awayTeam, String homeTeam, int awayScore, int homeScore,
                            String dateTime, String awaySeed, String homeSeed) {
            this.awayTeam  = awayTeam;
            this.homeTeam  = homeTeam;
            this.awayScore = awayScore;
            this.homeScore = homeScore;
            this.dateTime  = dateTime;
            this.awaySeed  = awaySeed;
            this.homeSeed  = homeSeed;
        }
    }

    /** 单节得分 */
    public static class QuarterScore {
        public int quarter;
        public int awayPoints;
        public int homePoints;

        public QuarterScore(int quarter, int awayPoints, int homePoints) {
            this.quarter    = quarter;
            this.awayPoints = awayPoints;
            this.homePoints = homePoints;
        }
    }

    /** 球员高光：得分、篮板、助攻 */
    public static class PlayerHighlight {
        public String name;
        public int pts;
        public int reb;
        public int ast;

        public PlayerHighlight(String name, int pts, int reb, int ast) {
            this.name = name;
            this.pts  = pts;
            this.reb  = reb;
            this.ast  = ast;
        }
    }

    /** 全队投篮、篮板、助攻、命中率等综合数据 */
    public static class TeamStats {
        public int awayPts;
        public int homePts;
        public int awayReb;
        public int homeReb;
        public int awayAst;
        public int homeAst;
        public double awayFgPct;
        public double homeFgPct;
        public double awayTpPct;
        public double homeTpPct;

        public TeamStats(int awayPts, int homePts, int awayReb, int homeReb,
                         int awayAst, int homeAst, double awayFgPct, double homeFgPct,
                         double awayTpPct, double homeTpPct) {
            this.awayPts   = awayPts;
            this.homePts   = homePts;
            this.awayReb   = awayReb;
            this.homeReb   = homeReb;
            this.awayAst   = awayAst;
            this.homeAst   = homeAst;
            this.awayFgPct = awayFgPct;
            this.homeFgPct = homeFgPct;
            this.awayTpPct = awayTpPct;
            this.homeTpPct = homeTpPct;
        }
    }

    /** 单队投篮命中/未中数据 */
    public static class TeamShot {
        public String team;
        public int made;
        public int attempt;
        public double pct;

        public TeamShot(String team, int made, int attempt, double pct) {
            this.team    = team;
            this.made    = made;
            this.attempt = attempt;
            this.pct     = pct;
        }
    }

    /** 投篮热图整体数据 */
    public static class ShotChart {
        public TeamShot away;
        public TeamShot home;

        public ShotChart(TeamShot away, TeamShot home) {
            this.away = away;
            this.home = home;
        }
    }
}
