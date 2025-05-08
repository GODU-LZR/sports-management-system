package com.example.event.dao.basketball;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 篮球赛事球员详情实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("event_basketball_match_player_details")
public class BasketballMatchPlayerDetails {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 球员ID
     */
    @TableField("player_id")
    private Integer playerId;

    /**
     * 赛事ID
     */
    @TableField("match_id")
    private String matchId;

    /**
     * 球队ID
     */
    @TableField("team_id")
    private Integer teamId;

    /**
     * 球员名称
     */
    @TableField("name")
    private String name;

    /**
     * 上场时间
     */
    @TableField("time_played")
    private String timePlayed;

    /**
     * 得分
     */
    @TableField("points")
    private Integer points;

    /**
     * 助攻
     */
    @TableField("assists")
    private Integer assists;

    /**
     * 篮板
     */
    @TableField("rebounds")
    private Integer rebounds;

    /**
     * 投篮命中数
     */
    @TableField("field_goals_made")
    private Integer fieldGoalsMade;

    /**
     * 投篮尝试数
     */
    @TableField("field_goals_attempted")
    private Integer fieldGoalsAttempted;

    /**
     * 比赛时间
     */
    @TableField("match_time")
    private LocalDateTime matchTime;
}