package com.example.user.mapper;

import com.example.user.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO sys_user (id, user_code, username, password, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted) " +
            "VALUES (#{id}, #{userCode}, #{username}, #{password}, #{email}, #{avatar}, #{realName}, #{status}, #{banEndTime}, #{createTime}, #{updateTime}, #{isDeleted})")
    int insert(User user);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE sys_user SET username = #{username}, password = #{password}, email = #{email}, avatar = #{avatar}, real_name = #{realName}, " +
            "status = #{status}, ban_end_time = #{banEndTime}, update_time = #{updateTime}, is_deleted = #{isDeleted} WHERE id = #{id}")
    int updateById(User user);

    @Select("SELECT id, user_code, username, password, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted FROM sys_user WHERE id = #{id}")
    User selectById(@Param("id") Long id);

    @Select("SELECT id, user_code, username, password, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted FROM sys_user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT id, user_code, username, password, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted FROM sys_user WHERE email = #{email}")
    User selectByEmail(@Param("email") String email);

    @Select("SELECT id, user_code, username, password, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted FROM sys_user")
    List<User> selectList();
}
