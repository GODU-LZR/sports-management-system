package com.example.event.dao.basketball;

import lombok.Data;

import java.io.Serializable;

@Data // Lombok 注解（需保留 lombok 依赖）
public class BasketballTeamStats implements Serializable { // 实现序列化
    private String matchId;  // 移除 MyBatis-Plus 注解
    private Long teamId;     // 移除 MyBatis-Plus 注解
    private Integer totalPoints;
    private Integer rebounds;
    private Integer assists;
    private Double fgPercent;
    private Double threePtPercent;
}