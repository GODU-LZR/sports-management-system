package com.example.event.dao.basketball;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("event_basketball_team")
public class BasketballTeam {
    @TableId(type = IdType.AUTO)
    @TableField("team_id")
    private Long teamId;

    @TableField("team_name")
    private String teamName;

    @TableField("conference")
    private String conference;

    @TableField("rank_position")
    private Integer rankPosition;

    @TableField("salary_cap")
    private BigDecimal salaryCap;
}