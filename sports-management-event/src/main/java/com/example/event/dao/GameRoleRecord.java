package com.example.event.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data // Lombok 注解，自动生成 Getters, Setters, toString, equals, hashCode
@TableName("game_role_record") // 指定数据库表名
public class GameRoleRecord {

    @TableId(value = "id", type = IdType.AUTO) // 指定主键和自增策略
    private Long id;

    @TableField("user_id") // 指定数据库字段名
    private Long userId;

    @TableField("game_id") // 指定数据库字段名
    private Long gameId;

    @TableField("role") // 指定数据库字段名
    private Integer role; // 存储 GameRole 枚举的 code

    @TableField("create_time")
    private LocalDateTime createTime; // 或者使用 java.time.LocalDateTime

    @TableField("update_time")
    private LocalDateTime updateTime;

    // 如果不使用 Lombok，手动添加 Getters 和 Setters 方法
    /*
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    */
}
