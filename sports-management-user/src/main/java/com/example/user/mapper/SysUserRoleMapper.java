package com.example.user.mapper;

import com.example.user.pojo.SysUserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {

    @Insert("INSERT INTO sys_user_role (id, user_id, role_id, create_time, creator_id) " +
            "VALUES (#{id}, #{userId}, #{roleId}, #{createTime}, #{creatorId})")
    int insert(SysUserRole sysUserRole);

    @Delete("DELETE FROM sys_user_role WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id, role_id, create_time, creator_id FROM sys_user_role WHERE id = #{id}")
    SysUserRole selectById(@Param("id") Long id);

    @Select("SELECT id, user_id, role_id, create_time, creator_id FROM sys_user_role WHERE user_id = #{userId}")
    List<SysUserRole> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id, role_id, create_time, creator_id FROM sys_user_role WHERE role_id = #{roleId}")
    List<SysUserRole> selectByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT id, user_id, role_id, create_time, creator_id FROM sys_user_role")
    List<SysUserRole> selectList();
}
