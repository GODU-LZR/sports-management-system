package com.example.event.service;

import java.util.List;
import java.util.Map;

/**
 * 比赛服务接口
 * 处理比赛相关的业务逻辑
 */
public interface MatchService {
    /**
     * 检查比赛是否存在
     * 
     * @return 是否存在
     */
    boolean existsById();
    
    /**
     * 获取比赛的基本信息数据
     *
     * @param matchId 比赛id
     * @param userId  用户id
     * @return 比赛基本信息
     */
    Map<String, Object> getMatchData(String matchId, Long userId);

    /**
     * 获取比赛的赛段得分信息
     *
     * @param matchId 比赛id
     * @param userId  用户id
     * @return 赛段得分信息
     */
    List<Map<String, Object>> getQuartersData(String matchId, Long userId);

    /**
     * 获取比赛的队伍得分信息
     *
     * @param matchId 比赛id
     * @param userId  用户id
     * @return 队伍得分信息
     */
    List<Map<String, Object>> getTeamStatsData(String matchId, Long userId);

    /**
     * 获取比赛队伍球员的得分信息
     *
     * @param matchId 比赛id
     * @param userId  用户id
     * @return 球员得分信息
     */
    List<List<Map<String, Object>>> getPlayersData(String matchId, Long userId);

    /**
     * 修改比赛的基本信息数据
     *
     * @param matchId 比赛id
     * @param form    修改表单
     * @param userId  用户id
     * @return 是否修改成功
     */
    boolean updateMatchData(String matchId, Map<String, Object> form, Long userId);

    /**
     * 修改比赛的赛段得分信息
     *
     * @param matchId 比赛id
     * @param form    修改表单
     * @param userId  用户id
     * @return 是否修改成功
     */
    boolean updateQuartersData(String matchId, List<Map<String, Object>> form, Long userId);

    /**
     * 修改比赛的队伍得分信息数据
     *
     * @param matchId 比赛id
     * @param form    修改表单
     * @param userId  用户id
     * @return 是否修改成功
     */
    boolean updateTeamStatsData(String matchId, List<Map<String, Object>> form, Long userId);

    /**
     * 修改球员得分信息数据
     *
     * @param matchId 比赛id
     * @param form    修改表单
     * @param userId  用户id
     * @return 是否修改成功
     */
    boolean updatePlayersData(String matchId, Map<String, Object> form, Long userId);

    /**
     * 切换比赛关注状态
     *
     * @param matchId 比赛id
     * @param userId  用户id
     * @return 是否操作成功
     */
    boolean toggleFollowStatus(String matchId, Long userId);

    /**
     * 获取比赛的球员得分信息（公开接口）
     *
     * @param matchId 比赛id
     * @return 球员得分信息
     */
    List<List<Map<String, Object>>> getSportMatchPlayersData(String matchId);
}
