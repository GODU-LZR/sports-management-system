package com.example.event.DTO.vo;

import lombok.Data;

import java.util.List;

/**
 * 比赛VO对象
 */
@Data
public class MatchVO {
    
    /**
     * 比赛ID
     */
    private String matchId;
    
    /**
     * 体育项目
     */
    private String sport;
    
    /**
     * 客队
     */
    private String awayTeam;
    
    /**
     * 主队
     */
    private String homeTeam;
    
    /**
     * 客队得分
     */
    private Integer awayTeamScore;
    
    /**
     * 主队得分
     */
    private Integer homeTeamScore;
    
    /**
     * 场地名称
     */
    private String venueName;
    
    /**
     * 开始时间
     */
    private String startTime;
    
    /**
     * 结束时间
     */
    private String endTime;
    
    /**
     * 是否关注(1-已关注，0-未关注)
     */
    private Integer isFollowed;
    
    /**
     * 赛段
     */
    private Integer phase;
    
    /**
     * 获胜方
     */
    private String winner;
    
    /**
     * 负责人
     */
    private String responsiblePerson;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 备注
     */
    private String note;
    
    /**
     * 裁判名称列表
     */
    private List<String> refereeName;
}