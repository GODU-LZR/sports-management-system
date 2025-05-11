package com.example.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.event.dao.Game;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 赛事数据访问接口
 */
@Mapper
public interface GameMapper extends BaseMapper<Game> {
    
    /**
     * 根据体育项目ID获取赛事列表数据（分页）
     * 
     * @param page 分页参数
     * @param sportId 体育项目ID
     * @param name 赛事名称（可选）
     * @param state 赛事状态（可选）
     * @param registerTime 报名时间（可选）
     * @param time 举行时间（可选）
     * @return 赛事列表分页数据
     */

    IPage<Game> getCompetitionData(Page<Game> page,
                                   @Param("sportId") Long sportId,
                                   @Param("name") String name,
                                   @Param("state") String state,
                                   @Param("registerTime") LocalDateTime registerTime,
                                   @Param("time") LocalDateTime time);

    /**
     * 获取赛事总条数
     * 
     * @param sportId 体育项目ID
     * @return 赛事总条数
     */
    @Select("SELECT COUNT(*) FROM game WHERE sport_id = #{sportId}")
    int getCompetitionCount(@Param("sportId") Long sportId);
    
    /**
     * 获取我的赛事列表（分页）
     * 
     * @param page 分页参数
     * @param userId 用户ID
     * @param reviewStatus 审核状态
     * @return 我的赛事列表分页数据
     */
    @Select("SELECT * FROM game WHERE creator_id = #{userId} AND review_status = #{reviewStatus} ORDER BY game_id")
    IPage<Game> getMyCompetitionData(Page<Game> page, 
                                    @Param("userId") Long userId, 
                                    @Param("reviewStatus") Integer reviewStatus);
    
    /**
     * 获取我的赛事总条数
     * 
     * @param userId 用户ID
     * @param reviewStatus 审核状态
     * @return 我的赛事总条数
     */
    @Select("SELECT COUNT(*) FROM game WHERE creator_id = #{userId} AND review_status = #{reviewStatus}")
    int getMyCompetitionCount(@Param("userId") Long userId, @Param("reviewStatus") Integer reviewStatus);
}