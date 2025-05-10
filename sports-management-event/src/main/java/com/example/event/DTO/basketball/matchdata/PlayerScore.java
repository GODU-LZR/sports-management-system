package com.example.event.DTO.basketball.matchdata;

import lombok.Data;

/**
 * 球员得分数据
 * 用于存储和展示球员的得分统计信息
 * 包含球员姓名和对应的分数值
 */
@Data
public class PlayerScore {
    public Integer teamId;
    public String name;  // 球员姓名
    public int score;    // 得分值
    public String avater_url;

    public PlayerScore(String name, int score, Integer teamId) {
        this.name = name;
        this.score = score;
        this.teamId=teamId;
    }
    public PlayerScore(String name, int score, Integer teamId,String avater_url) {
        this.name = name;
        this.score = score;
        this.teamId=teamId;
        this.avater_url=avater_url;
    }
}
