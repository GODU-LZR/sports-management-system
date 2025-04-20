package com.example.event.dao.basketball;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("event_basketball_team_statistics")
public class TeamStats {
    @TableId("match_id")
    private String matchId;

    @TableId("team_id")
    private Long teamId;

    @TableField("total_points")
    private Integer totalPoints;

    @TableField("rebounds")
    private Integer rebounds;

    @TableField("assists")
    private Integer assists;

    @TableField("fg_percent")
    private Double fgPercent;

    @TableField("three_pt_percent")
    private Double threePtPercent;
}