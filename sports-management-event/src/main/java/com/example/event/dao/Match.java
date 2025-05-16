package com.example.event.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("match")
public class Match {

    @TableId(value = "match_id", type = IdType.INPUT)
    private String matchId;

    @TableField("game_id")
    private Long gameId;

    @TableField("sport")
    private String sport;

    @TableField("away_team")
    private String awayTeam;

    @TableField("home_team")
    private String homeTeam;

    @TableField("away_team_score")
    private Integer awayTeamScore=0;

    @TableField("home_team_score")
    private Integer homeTeamScore=0;

    @TableField("venue_name")
    private String venueName;

    // 👇 新增字段
    @TableField("away_team_id")
    private String awayTeamId;

    @TableField("home_team_id")
    private String homeTeamId;

    @TableField("venue_id")
    private String venueId;



    // 👆 新增结束

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("phase")
    private Integer phase;

    @TableField(value = "winner", fill = FieldFill.INSERT)
    private String winner;

    @TableField("note")
    private String note;


}