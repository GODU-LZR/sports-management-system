package com.example.event.DTO;

/**
 * 单队综合统计数据
 */
public class TeamStatistics {
    public String name;
    public int pts;
    public int reb;
    public int ast;
    public double fgPct;
    public double tpPct;

    public TeamStatistics(String name, int pts, int reb, int ast, double fgPct, double tpPct) {
        this.name = name;
        this.pts = pts;
        this.reb = reb;
        this.ast = ast;
        this.fgPct = fgPct;
        this.tpPct = tpPct;
    }
}
