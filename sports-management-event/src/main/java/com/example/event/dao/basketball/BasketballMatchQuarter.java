package com.example.event.dao.basketball;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@TableName("event_basketball_match_quarters")
public class BasketballMatchQuarter {
    @TableId(type = IdType.INPUT)
    public MatchQuarterKey id;

    @TableField("q1")
    private Integer q1;

    @TableField("q2")
    private Integer q2;

    @TableField("q3")
    private Integer q3;

    @TableField("q4")
    private Integer q4;
}

