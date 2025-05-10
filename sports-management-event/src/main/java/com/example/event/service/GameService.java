package com.example.event.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.event.dao.Game;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 赛事服务接口
 */
public interface GameService {
    
    /**
     * 获取赛事基本信息
     * 
     * @param gameId 赛事ID
     * @return 赛事基本信息
     */
    Map<String, Object> getGameData(Long gameId);
    
    /**
     * 获取赛事对应的比赛列表
     * 
     * @param gameId 赛事ID
     * @return 比赛列表
     */
    List<Map<String, Object>> getMatches(Long gameId);
    
    /**
     * 获取我的赛事列表（分页）
     * 
     * @param page 页码
     * @param reviewStatus 审核状态
     * @param userId 用户ID
     * @return 我的赛事列表
     */
    List<Map<String, Object>> getMyCompetitionData(Integer page, Integer reviewStatus, Long userId);
    
    /**
     * 获取我的赛事总条数
     * 
     * @param reviewStatus 审核状态
     * @param userId 用户ID
     * @return 总条数
     */
    Integer getMyCompetitionCount(Integer reviewStatus, Long userId);
    
    /**
     * 获取我的赛事基本信息
     * 
     * @param gameId 赛事ID
     * @param userId 用户ID
     * @return 赛事基本信息
     */
    Map<String, Object> getMyGameData(Long gameId, Long userId);
    
    /**
     * 获取我的赛事对应的比赛列表
     * 
     * @param gameId 赛事ID
     * @param userId 用户ID
     * @return 比赛列表
     */
    List<Map<String, Object>> getMyMatches(Long gameId, Long userId);
    
    /**
     * 根据体育项目ID获取赛事列表（分页）
     * 
     * @param page 页码
     * @param sportId 体育项目ID
     * @param name 赛事名称（可选）
     * @param state 赛事状态（可选）
     * @param registerTime 报名时间（可选）
     * @param time 举行时间（可选）
     * @return 赛事列表
     */
    IPage<Game> getCompetitionData(Integer page, Long sportId, String name, String state,
                                   LocalDateTime registerTime, LocalDateTime time);
    
    /**
     * 获取赛事总条数
     * 
     * @param sportId 体育项目ID
     * @return 赛事总条数
     */
    int getCompetitionCount(Long sportId);
    
    /**
     * 创建赛事
     * 
     * @param game 赛事信息
     * @return 创建的赛事ID
     */
    Long createGame(Game game);
    
    /**
     * 更新赛事信息
     * 
     * @param game 赛事信息
     * @return 是否更新成功
     */
    boolean updateGame(Game game);
    
    /**
     * 删除赛事
     * 
     * @param gameId 赛事ID
     * @return 是否删除成功
     */
    boolean deleteGame(Long gameId);
    
    /**
     * 更新赛事审核状态
     * 
     * @param gameId 赛事ID
     * @param reviewStatus 审核状态
     * @return 是否更新成功
     */
    boolean updateReviewStatus(Long gameId, Integer reviewStatus);
}