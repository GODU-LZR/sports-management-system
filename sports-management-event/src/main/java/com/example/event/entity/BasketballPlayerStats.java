package com.example.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 篮球球员统计数据实体
 */
@Data
@TableName("basketball_player_stats")
public class BasketballPlayerStats implements Serializable {

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
     * 球员ID
     */
    private Long playerId;

    /**
     * 球队ID
     */
    private Long teamId;

    /**
     * 上场时间(分钟)
     */
    private Integer minutesPlayed;

    /**
     * 得分
     */
    private Integer points;

    /**
     * 篮板
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
     * 个人犯规
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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}