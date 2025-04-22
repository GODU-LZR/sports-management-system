package com.example.event.dao.basketball;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchQuarterKey implements Serializable {
    @TableField("match_id")
    private String matchId;

    @TableField("team_id")
    private Long teamId;
}
