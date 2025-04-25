package com.example.event.DTO;

import com.example.event.dao.basketball.BasketballShotChart;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.type.JdbcType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 投篮热图整体数据
 */
@NoArgsConstructor
@Data

public class ShotChart {

    String matchId; // 比赛ID
    Integer awayteamId; // 客队ID
    Integer hometeamId; // 主队ID
    public TeamShot away; // 客队投篮统计信息
    public TeamShot home; // 主队投篮统计信息

    @Data // Lombok 注解，自动生成 getter、setter、equals、hashCode 和 toString 方法
    public static class TeamShot {
        public Integer team; // 球队名称（例如：'客队', '主队'）
        public int made; // 进球数
        public int attempt; // 出手次数
        public double pct; // 投篮命中率

        // TeamShot 类的构造方法
        public TeamShot( int made, int attempt, double pct) {

            this.made = made;
            this.attempt = attempt;
            this.pct = pct;
        }
    }

    /**
     * 获取主队ID
     */
    public static Integer getHometeamId() {
        // 实现获取主队ID的逻辑
        return 1; // 假设主队ID为1，根据实际业务逻辑调整
    }



    // 这两个方法需要根据你的实际业务逻辑来实现，
    // 以确定哪个 Team ID 代表客队，哪个代表主队。
    // 你可以选择将它们放在 ShotChart 类中作为静态方法，
    // 或者通过其他方式在 convertToShotChart 方法中访问到这些信息。
    public ShotChart(TeamShot away, TeamShot home) {
        this.away = away;
        this.home = home;
    }

    /**
     * 获取客队ID
     */
    public static Integer getAwayteamId() {
        // 实现获取客队ID的逻辑
        return 2; // 假设客队ID为2，根据实际业务逻辑调整
    }

}