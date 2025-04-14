package com.example.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 体育赛事基础实体
 */
@NoArgsConstructor
@Data//基类使用回影响子类的hashcode和equals。构造函数
public  class SportEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛事ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 赛事名称
     */
    private String name;

    /**
     * 赛事描述
     */
    private String description;

    /**
     * 赛事类型（如：联赛、杯赛、友谊赛等）
     */
    private String eventType;

    /**
     * 赛事级别（如：国际、国家、省级、市级等）
     */
    private String eventLevel;

    /**
     * 赛事状态（0-未开始，1-进行中，2-已结束）
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 赛事地点
     */
    private String location;

    /**
     * 主办方
     */
    private String organizer;

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

    /**
     * 获取运动类型
     */
    public String getSportType() {
        return null;
    }
}