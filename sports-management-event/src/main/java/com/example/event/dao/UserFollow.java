package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户关注比赛关联实体
 */
@Data
@TableName("user_follow")
public class UserFollow {

    /**
     * 关注ID
     */
    @TableId(value = "follow_id", type = IdType.AUTO)
    private Long followId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 比赛ID
     */
    @TableField("match_id")
    private String matchId;
}