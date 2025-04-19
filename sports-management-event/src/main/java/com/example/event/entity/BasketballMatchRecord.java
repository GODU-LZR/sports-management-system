package com.example.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 篮球比赛记录实体
 */
@Data
@TableName("basketball_match_record")
public class BasketballMatchRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 赛事ID
     */
    private Long eventId;

    /**
     * 主队ID
     */
    private Long homeTeamId;

    /**
     * 客队ID
     */
    private Long awayTeamId;

    /**
     * 比赛时间
     */
    private LocalDateTime matchTime;

    /**
     * 比赛场地
     */
    private String venue;

    /**
     * 比赛状态（0-未开始，1-进行中，2-已结束）
     */
    private Integer status;

    /**
     * 当前节数
     */
    private Integer currentQuarter;

    /**
     * 主队得分
     */
    private Integer homeScore;

    /**
     * 客队得分
     */
    private Integer awayScore;

    /**
     * 主队第一节得分
     */
    private Integer homeQ1Score;

    /**
     * 客队第一节得分
     */
    private Integer awayQ1Score;

    /**
     * 主队第二节得分
     */
    private Integer homeQ2Score;

    /**
     * 客队第二节得分
     */
    private Integer awayQ2Score;

    /**
     * 主队第三节得分
     */
    private Integer homeQ3Score;

    /**
     * 客队第三节得分
     */
    private Integer awayQ3Score;

    /**
     * 主队第四节得分
     */
    private Integer homeQ4Score;

    /**
     * 客队第四节得分
     */
    private Integer awayQ4Score;

    /**
     * 主队加时赛得分
     */
    private Integer homeOtScore;

    /**
     * 客队加时赛得分
     */
    private Integer awayOtScore;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}