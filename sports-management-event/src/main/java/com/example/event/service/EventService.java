package com.example.event.service;

import com.example.event.entity.SportEvent;

/**
 * 赛事服务接口
 */
public interface EventService<T extends SportEvent> {
    
    /**
     * 创建赛事
     */
    T createEvent(T event);
    
    /**
     * 更新赛事
     */
    T updateEvent(T event);
    
    /**
     * 删除赛事
     */
    boolean deleteEvent(Long eventId);
    
    /**
     * 获取赛事详情
     */
    T getEventById(Long eventId);
    
    /**
     * 开始赛事
     */
    boolean startEvent(Long eventId);
    
    /**
     * 结束赛事
     */
    boolean endEvent(Long eventId);
}