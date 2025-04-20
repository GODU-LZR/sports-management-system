package com.example.event.DTO;

/**
 * 单队比赛概览
 */
public class TeamSummary {
    public String team;
    public int score;
    public String seed;
    public int votes;
    public double votePct;

    public TeamSummary(String team, int score, String seed, int votes, double votePct) {
        this.team = team;
        this.score = score;
        this.seed = seed;
        this.votes = votes;
        this.votePct = votePct;
    }
}
