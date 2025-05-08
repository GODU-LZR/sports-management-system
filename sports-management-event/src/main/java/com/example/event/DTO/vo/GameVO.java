package com.example.event.DTO.vo;

import lombok.Data;

/**
 * 赛事VO对象
 */
@Data
public class GameVO {
    
    /**
     * 赛事ID
     */
    private Long gameId;
    
    /**
     * 赛事名称
     */
    private String name;
    
    /**
     * 体育项目
     */
    private String sport;
    
    /**
     * 负责人
     */
    private String responsiblePeople;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 报名开始时间
     */
    private String registerStartTime;
    
    /**
     * 报名结束时间
     */
    private String registerEndTime;
    
    /**
     * 赛事开始时间
     */
    private String startTime;
    
    /**
     * 赛事结束时间
     */
    private String endTime;
    
    /**
     * 备注信息
     */
    private String note;
    
    /**
     * 匹配模式
     */
    private Integer mode;
}