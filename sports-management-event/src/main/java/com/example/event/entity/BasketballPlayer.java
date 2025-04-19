package com.example.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 篮球球员实体
 */
@Data
@TableName("basketball_player")
public class BasketballPlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 球员ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属球队ID
     */
    private Long teamId;

    /**
     * 球员姓名
     */
    private String name;

    /**
     * 球衣号码
     */
    private Integer jerseyNumber;

    /**
     * 场上位置
     */
    private String position;

    /**
     * 身高(cm)
     */
    private BigDecimal height;

    /**
     * 体重(kg)
     */
    private BigDecimal weight;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 国籍
     */
    private String nationality;

    /**
     * 照片
     */
    private String photo;

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