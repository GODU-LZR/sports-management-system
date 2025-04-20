package com.example.event.DTO;

/**
 * 单队投篮命中/未中数据
 */
public class TeamShot {
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
