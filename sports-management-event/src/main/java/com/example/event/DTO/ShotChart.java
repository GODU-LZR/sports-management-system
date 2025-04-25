package com.example.event.DTO;

import com.example.event.dao.basketball.BasketballShotChart;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 投篮热图整体数据
 */
public class ShotChart {
    Integer awayteamId;
    Integer hometeamId;
    public TeamShot away;
    public TeamShot home;

    public ShotChart(TeamShot away, TeamShot home) {
        this.away = away;
        this.home = home;
    }

    /**
     * 从 BasketballShotChart 列表转换为 ShotChart 对象
     *
     * @param shotChartList BasketballShotChart 列表
     * @return ShotChart 对象
     */
    public static ShotChart convertToShotChart(List<BasketballShotChart> shotChartList) {
        if (shotChartList == null || shotChartList.isEmpty()) {
            return new ShotChart(null, null);
        }

        // 使用 Map 按球队 ID 分组数据
        Map<Long, List<BasketballShotChart>> groupedByTeam = shotChartList.stream()
                .collect(Collectors.groupingBy(BasketballShotChart::getTeamId));

        TeamShot awayTeamShot = null;
        TeamShot homeTeamShot = null;

        if (groupedByTeam.containsKey(getAwayTeamId())) { // 需要在 ShotChart 类中实现或能访问 getAwayTeamId()
            List<BasketballShotChart> awayData = groupedByTeam.get(getAwayTeamId());
            int totalMade = awayData.stream().mapToInt(BasketballShotChart::getMadeShots).sum();
            int totalAttempted = awayData.stream().mapToInt(BasketballShotChart::getAttemptedShots).sum();
            double percentage = totalAttempted == 0 ? 0 : awayData.get(0).getShotPercent().doubleValue();
            awayTeamShot = new TeamShot("客队", totalMade, totalAttempted, percentage);
        }

        if (groupedByTeam.containsKey(getHomeTeamId())) { // 需要在 ShotChart 类中实现或能访问 getHomeTeamId()
            List<BasketballShotChart> homeData = groupedByTeam.get(getHomeTeamId());
            int totalMade = homeData.stream().mapToInt(BasketballShotChart::getMadeShots).sum();
            int totalAttempted = homeData.stream().mapToInt(BasketballShotChart::getAttemptedShots).sum();
            double percentage = totalAttempted == 0 ? 0 : homeData.get(0).getShotPercent().doubleValue();
            homeTeamShot = new TeamShot("主队", totalMade, totalAttempted, percentage);
        }

        return new ShotChart(awayTeamShot, homeTeamShot);
    }

    /**
     * 单队投篮命中/未中数据
     */
    @Data
    public static class TeamShot {
        public String team;
        public int made;
        public int attempt;
        public double pct;

        public TeamShot(String team, int made, int attempt, double pct) {
            this.team = team;
            this.made = made;
            this.attempt = attempt;
            this.pct = pct;
        }
    }

    // 这两个方法需要根据你的实际业务逻辑来实现，
    // 以确定哪个 Team ID 代表客队，哪个代表主队。
    // 你可以选择将它们放在 ShotChart 类中作为静态方法，
    // 或者通过其他方式在 convertToShotChart 方法中访问到这些信息。
    private static Long getAwayTeamId() {
        // TODO: 实现获取客队 ID 的逻辑
        return 1L;
    }

    private static Long getHomeTeamId() {
        // TODO: 实现获取主队 ID 的逻辑
        return 2L; // 示例值，请替换为你的实际逻辑
    }
}