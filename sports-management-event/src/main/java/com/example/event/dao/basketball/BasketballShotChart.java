package com.example.event.dao.basketball;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 篮球投篮图数据实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("event_basketball_shot_chart")
public class BasketballShotChart implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 比赛ID
     */
    @TableId(value = "match_id", type = IdType.INPUT)
    private String matchId;

    /**
     * 球队ID
     */
    @TableField(value = "team_id")
    private Integer teamId;

    /**
     * 命中数
     */
    @TableField("made_shots")
    private Integer madeShots;

    /**
     * 出手次数
     */
    @TableField("attempted_shots")
    private Integer attemptedShots;

    /**
     * 命中率
     */
    @TableField("shot_percent")
    private BigDecimal shotPercent;

    @TableField(exist = false) // 表示该字段在数据库表中不存在
    private String extraInfo;

    // 可以添加其他不需要映射到数据库的字段，并使用 @TableField(exist = false) 注解
}