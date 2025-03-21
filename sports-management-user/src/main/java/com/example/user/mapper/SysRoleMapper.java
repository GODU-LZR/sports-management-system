package com.example.user.mapper;

import com.example.user.pojo.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    @Insert("INSERT INTO sys_role (id, role_code, role_name, description, create_time, update_time, is_deleted) " +
            "VALUES (#{id}, #{roleCode}, #{roleName}, #{description}, #{createTime}, #{updateTime}, #{isDeleted})")
    int insert(SysRole sysRole);

    @Delete("DELETE FROM sys_role WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE sys_role SET role_code = #{roleCode}, role_name = #{roleName}, description = #{description}, update_time = #{updateTime}, is_deleted = #{isDeleted} WHERE id = #{id}")
    int updateById(SysRole sysRole);

    @Select("SELECT id, role_code, role_name, description, create_time, update_time, is_deleted FROM sys_role WHERE id = #{id}")
    SysRole selectById(@Param("id") Long id);

    @Select("SELECT id, role_code, role_name, description, create_time, update_time, is_deleted FROM sys_role WHERE role_code = #{roleCode}")
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    @Select("SELECT id, role_code, role_name, description, create_time, update_time, is_deleted FROM sys_role")
    List<SysRole> selectList();
}
