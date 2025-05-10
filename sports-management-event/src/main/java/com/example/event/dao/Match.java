package com.example.event.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 比赛实体类
 */
@Data
@TableName("match")
public class Match {

    /**
     * 比赛ID
     */
    @TableId(value = "match_id", type = IdType.INPUT)
    private String matchId;

    /**
     * 赛事ID
     */
    @TableField("game_id")
    private Long gameId;

    /**
     * 体育项目
     */
    @TableField("sport")
    private String sport;

    /**
     * 客队
     */
    @TableField("away_team")
    private String awayTeam;

    /**
     * 主队
     */
    @TableField("home_team")
    private String homeTeam;

    /**
     * 客队得分
     */
    @TableField("away_team_score")
    private Integer awayTeamScore;

    /**
     * 主队得分
     */
    @TableField("home_team_score")
    private Integer homeTeamScore;

    /**
     * 场地名称
     */
    @TableField("venue_name")
    private String venueName;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 赛段
     */
    @TableField("phase")
    private Integer phase;

    /**
     * 获胜方
     */
    @TableField("winner")
    private String winner;

    /**
     * 负责人
     */
    @TableField("responsible_person")
    private String responsiblePerson;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 备注
     */
    @TableField("note")
    private String note;

    /**
     * 裁判名称列表，以JSON格式存储
     */
    @TableField("referee_name")
    private String refereeName;
}