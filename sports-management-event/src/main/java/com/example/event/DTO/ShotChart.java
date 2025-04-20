package com.example.event.DTO;

/**
 * 投篮热图整体数据
 */
public class ShotChart {
    public TeamShot away;
    public TeamShot home;

    public ShotChart(TeamShot away, TeamShot home) {
        this.away = away;
        this.home = home;
    }
}
