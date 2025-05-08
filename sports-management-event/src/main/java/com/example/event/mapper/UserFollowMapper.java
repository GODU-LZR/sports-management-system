package com.example.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.UserFollow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户关注比赛关联数据访问接口
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
    
    /**
     * 添加用户关注
     * 
     * @param userId 用户ID
     * @param matchId 比赛ID
     * @return 影响行数
     */
    @Insert("INSERT INTO user_follow(user_id, match_id) VALUES(#{userId}, #{matchId})")
    int addUserFollow(@Param("userId") Long userId, @Param("matchId") String matchId);
    
    /**
     * 取消用户关注
     * 
     * @param userId 用户ID
     * @param matchId 比赛ID
     * @return 影响行数
     */
    @Delete("DELETE FROM user_follow WHERE user_id = #{userId} AND match_id = #{matchId}")
    int deleteUserFollow(@Param("userId") Long userId, @Param("matchId") String matchId);
}