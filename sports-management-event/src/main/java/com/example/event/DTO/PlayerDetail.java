package com.example.event.DTO;

import lombok.Data;

/**
 * 球员详细数据结构
 * 用于存储和展示球员在比赛中的详细统计数据
 * 包含球员姓名、上场时间、得分、篮板、助攻、投篮命中数和投篮出手数
 */
@Data
public class PlayerDetail {
    public String name;    // 球员姓名
    public String minutes; // 上场时间
    public int pts;        // 得分
    public int reb;        // 篮板
    public int ast;        // 助攻
    public int fgMade;     // 投篮命中数
    public int fgAtt;      // 投篮出手数

    public PlayerDetail(String name, String minutes, int pts, int reb, int ast, int fgMade, int fgAtt) {
        this.name = name;
        this.minutes = minutes;
        this.pts = pts;
        this.reb = reb;
        this.ast = ast;
        this.fgMade = fgMade;
        this.fgAtt = fgAtt;
    }
}
