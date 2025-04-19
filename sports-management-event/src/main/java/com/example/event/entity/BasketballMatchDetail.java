package com.example.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 篮球比赛详细记录实体
 */
@Data
@TableName("basketball_match_detail")
public class BasketballMatchDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
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
     * 球员ID
     */
    private Long playerId;

    /**
     * 动作类型（得分、犯规、助攻、篮板等）
     */
    private String actionType;

    /**
     * 动作值（如得分类型：2分、3分、罚球）
     */
    private String actionValue;

    /**
     * 节次
     */
    private Integer quarter;

    /**
     * 节内时间
     */
    private String timeInQuarter;

    /**
     * 得分变化
     */
    private Integer scoreChange;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}