package com.example.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.event.dao.Sport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 体育项目数据访问接口
 */
@Mapper
public interface SportMapper extends BaseMapper<Sport> {
    
    /**
     * 获取带赛事的赛事项目数据
     * 
     * @return 带赛事的体育项目列表
     */
    @Select("SELECT s.sport_id, s.name, g.game_id, g.name as game_name " +
            "FROM sport s " +
            "LEFT JOIN game g ON s.sport_id = g.sport_id " +
            "ORDER BY s.sport_id")
    List<Map<String, Object>> getSportWithGames();
    
    /**
     * 获取不带赛事列表的赛事项目数据
     * 
     * @return 体育项目列表
     */
    @Select("SELECT sport_id, name FROM sport ORDER BY sport_id")
    List<Sport> getSportList();
}