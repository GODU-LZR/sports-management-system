package com.example.event.dao.basketball;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("event_basketabll_match")
public class Match {
    @TableId("match_id")
    private String matchId;

    @TableField("home_team_id")
    private Long homeTeamId;

    @TableField("away_team_id")
    private Long awayTeamId;

    @TableField("match_time")
    private LocalDateTime matchTime;

    // 非数据库字段需显式标注
    @TableField(exist = false)
    private Team homeTeam;

    @TableField(exist = false)
    private Team awayTeam;
}