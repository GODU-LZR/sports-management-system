package com.example.event.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 赛事实体类
 */
@Data
@TableName("game")
public class Game {

    /**
     * 赛事ID
     */
    @TableId(value = "game_id", type = IdType.AUTO)
    private Long gameId;

    /**
     * 赛事名称
     */
    @TableField("name")
    private String name;

    /**
     * 体育项目ID
     */
    @TableField("sport_id")
    private Long sportId;

    /**
     * 体育项目名称
     */
    @TableField("sport")
    private String sport;

    /**
     * 负责人
     */
    @TableField("responsible_people")
    private String responsiblePeople;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 报名开始时间
     */
    @TableField("register_start_time")
    private LocalDateTime registerStartTime;

    /**
     * 报名结束时间
     */
    @TableField("register_end_time")
    private LocalDateTime registerEndTime;

    /**
     * 赛事开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 赛事结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 备注信息
     */
    @TableField("note")
    private String note;

    /**
     * 匹配模式(0-系统按时间分配、1-系统按随机分配、2-用户自定义)
     */
    @TableField("mode")
    private Integer mode;

    /**
     * 审核状态(0-待审核、1-已通过、2-已否决、3-已撤销)
     */
    @TableField("review_status")
    private Integer reviewStatus;

    /**
     * 创建者ID
     */
    @TableField("creator_id")
    private Long creatorId;
}