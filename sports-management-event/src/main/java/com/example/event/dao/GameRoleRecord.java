package com.example.event.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.event.Enum.GameRole; // 1. 导入 GameRole 枚举
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("game_role_record")
public class GameRoleRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("game_id")
    private Long gameId;


    @TableField("role") // 数据库中的 "role" 列应该仍然是 INTEGER 类型，用来存储 GameRole 的 code
    private GameRole role; // 2. 将 role 字段的类型从 Integer 修改为 GameRole

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("team_id")
    private Long teamId;

    // Lombok 的 @Data 注解会自动生成 Getters 和 Setters
    // 如果不使用 Lombok，你需要手动为 GameRole role 添加 getter 和 setter:
    /*
    public GameRole getRole() { return role; }
    public void setRole(GameRole role) { this.role = role; }
    */
}