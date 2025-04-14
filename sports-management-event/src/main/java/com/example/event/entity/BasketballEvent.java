package com.example.event.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 篮球赛事实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basketball_event")
public class BasketballEvent extends SportEvent {
    
    /**
     * 赛事规则
     */
    private String rules;
    
    /**
     * 参赛队伍数量
     */
    private Integer teamCount;
    
    /**
     * 每场比赛节数
     */
    private Integer quartersPerMatch;
    
    /**
     * 每节时长(分钟)
     */
    private Integer minutesPerQuarter;
    
    @Override
    public String getSportType() {
        return "basketball";
    }
}