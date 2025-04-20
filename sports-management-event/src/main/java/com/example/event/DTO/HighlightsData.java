package com.example.event.DTO;

import java.util.List;
import lombok.Data;

/**
 * 球员高光数据分组
 * 用于存储和展示比赛中球员的高光表现数据
 * 包含得分(pt)、篮板(reb)和助攻(ast)三个维度的球员数据
 */
@Data
public class HighlightsData {
    public List<PlayerScore> pt;
    public List<PlayerScore> reb;
    public List<PlayerScore> ast;

    public HighlightsData(List<PlayerScore> pt, List<PlayerScore> reb, List<PlayerScore> ast) {
        this.pt = pt;
        this.reb = reb;
        this.ast = ast;
    }
}
