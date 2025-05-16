package com.example.event.dao.temp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("temp_gameandteam")
public class Gameandteam {
    @TableId("id")
    Long id;
    @TableId("team_id")
    Long game_id;
    @TableField("team_id")
    Long team_id;
}
