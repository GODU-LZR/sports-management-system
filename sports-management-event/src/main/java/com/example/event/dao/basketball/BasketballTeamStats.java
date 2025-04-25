package com.example.event.dao.basketball;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data // Lombok 注解（需保留 lombok 依赖）
@TableName("event_basketball_team_statistics") // 指定对应的数据库表名
public class BasketballTeamStats implements Serializable { // 实现序列化
    @TableId(value = "match_id") // 指定主键字段和数据库列名
    private String matchId;
    @TableId(value = "team_id") // 指定主键字段和数据库列名
    private Long teamId;
    @TableField("total_points") // 显式绑定字段名和列名
    private Integer totalPoints;
    @TableField("rebounds") // 显式绑定字段名和列名
    private Integer rebounds;
    @TableField("assists") // 显式绑定字段名和列名
    private Integer assists;
    @TableField("fg_percent") // 显式绑定字段名和列名
    private Double fgPercent;
    @TableField("three_pt_percent") // 显式绑定字段名和列名
    private Double threePtPercent;
}