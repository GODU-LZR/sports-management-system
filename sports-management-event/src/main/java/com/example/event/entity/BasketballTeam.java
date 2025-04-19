package com.example.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 篮球球队实体
 */
@Data
@TableName("basketball_team")
public class BasketballTeam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 球队ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 球队名称
     */
    private String name;

    /**
     * 球队logo
     */
    private String logo;

    /**
     * 教练名称
     */
    private String coach;

    /**
     * 球队描述
     */
    private String description;

    /**
     * 成立时间
     */
    private LocalDateTime foundedTime;

    /**
     * 主场
     */
    private String homeCourt;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer isDeleted;
}