package com.example.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 篮球球队统计数据实体
 */
@Data
@TableName("basketball_team_stats")
public class BasketballTeamStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 比赛ID
     */
    private Long matchId;

    /**
     * 球队ID
     */
    private Long teamId;

    /**
     * 总得分
     */
    private Integer points;

    /**
     * 总篮板
     */
    private Integer rebounds;

    /**
     * 前场篮板
     */
    private Integer offensiveRebounds;

    /**
     * 后场篮板
     */
    private Integer defensiveRebounds;

    /**
     * 助攻
     */
    private Integer assists;

    /**
     * 抢断
     */
    private Integer steals;

    /**
     * 盖帽
     */
    private Integer blocks;

    /**
     * 失误
     */
    private Integer turnovers;

    /**
     * 团队犯规
     */
    private Integer personalFouls;

    /**
     * 投篮命中
     */
    private Integer fieldGoalsMade;

    /**
     * 投篮出手
     */
    private Integer fieldGoalsAttempted;

    /**
     * 三分命中
     */
    private Integer threePointersMade;

    /**
     * 三分出手
     */
    private Integer threePointersAttempted;

    /**
     * 罚球命中
     */
    private Integer freeThrowsMade;

    /**
     * 罚球出手
     */
    private Integer freeThrowsAttempted;

    /**
     * 快攻得分
     */
    private Integer fastBreakPoints;

    /**
     * 内线得分
     */
    private Integer pointsInPaint;

    /**
     * 二次进攻得分
     */
    private Integer secondChancePoints;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}