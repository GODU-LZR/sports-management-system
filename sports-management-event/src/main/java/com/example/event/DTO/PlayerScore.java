package com.example.event.DTO;

import lombok.Data;

/**
 * 球员得分数据
 * 用于存储和展示球员的得分统计信息
 * 包含球员姓名和对应的分数值
 */
@Data
public class PlayerScore {
    public String name;  // 球员姓名
    public int score;    // 得分值

    public PlayerScore(String name, int score) {
        this.name = name;
        this.score = score;
    }
}
