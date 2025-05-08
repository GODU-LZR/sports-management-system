package com.example.event.service;

import com.example.event.DTO.vo.SportVO;

import java.util.List;
import java.util.Map;

/**
 * 体育项目服务接口
 */
public interface SportService {

    /**
     * 获取带赛事的赛事项目数据
     *
     * @return 带赛事的体育项目列表
     */
    List<SportVO> getSportWithGames();

    /**
     * 获取不带赛事列表的赛事项目数据
     *
     * @return 体育项目列表
     */
    List<SportVO> getSportList();

    /**
     * 获取赛事列表数据
     *
     * @param sportId 赛事项目id
     * @param page 页码
     * @param filter 过滤条件
     * @return 赛事列表数据
     */
    Map<String, Object> getCompetitionData(Integer sportId, Integer page, Map<String, Object> filter);

    /**
     * 获取赛事列表的总条数
     *
     * @param sportId 赛事项目id
     * @return 总条数
     */
    Integer getCompetitionCount(Integer sportId);
}