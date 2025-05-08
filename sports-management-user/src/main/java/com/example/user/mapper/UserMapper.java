package com.example.user.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.user.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户表 Mapper 接口
 * 注意：大部分查询和更新操作已添加 is_deleted = 0 条件，
 * 以排除已被软删除的用户。
 */
@Mapper
public interface UserMapper {

    /**
     * 插入新用户 (设置默认 is_deleted 为 0)
     * @param user 用户对象
     * @return 影响行数
     */
    @Insert("INSERT INTO sys_user (id, user_code, username, password, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted) " +
            "VALUES (#{id}, #{userCode}, #{username}, #{password}, #{email}, #{avatar}, #{realName}, #{status}, #{banEndTime}, #{createTime}, #{updateTime}, 0)") // 插入时默认 is_deleted = 0
    int insert(User user);

    /**
     * 物理删除用户 (请谨慎使用，通常应使用软删除)
     * @param id 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteByIdPhysical(@Param("id") Long id); // 重命名以区分软删除

    /**
     * 根据ID更新【未删除】用户的所有字段 (密码应在Service层加密)
     * @param user 包含更新信息的用户对象
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET username = #{username}, password = #{password}, email = #{email}, avatar = #{avatar}, real_name = #{realName}, " +
            "status = #{status}, ban_end_time = #{banEndTime}, update_time = NOW() " + // is_deleted 不应在此处更新
            "WHERE id = #{id} AND is_deleted = 0") // 添加条件
    int updateById(User user);

    /**
     * 根据ID查询【未删除】用户
     * @param id 用户ID
     * @return 用户对象，如果不存在或已删除则返回 null
     */
    @Select("SELECT id, user_code, username, password, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted " +
            "FROM sys_user WHERE id = #{id} AND is_deleted = 0") // 添加条件
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询【未删除】用户
     * @param username 用户名
     * @return 用户对象，如果不存在或已删除则返回 null
     */
    @Select("SELECT id, user_code, username, password, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted " +
            "FROM sys_user WHERE username = #{username} AND is_deleted = 0") // 添加条件
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询【未删除】用户
     * @param email 邮箱地址
     * @return 用户对象，如果不存在或已删除则返回 null
     */
    @Select("SELECT id, user_code, username, password, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted " +
            "FROM sys_user WHERE email = #{email} AND is_deleted = 0") // 添加条件
    User selectByEmail(@Param("email") String email);

    /**
     * 查询所有【未删除】的用户列表
     * @return 用户列表
     */
    @Select("SELECT id, user_code, username, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted " + // 优化：不需要查询密码
            "FROM sys_user WHERE is_deleted = 0") // 添加条件
    List<User> selectList();


    /**
     * 选择性更新【未删除】用户的字段 (根据传入对象的非null值)
     * @param user 包含要更新字段的用户对象 (ID 必传)
     * @return 影响行数
     */
    @Update({
            "<script>",
            "UPDATE sys_user",
            "<set>",
            "<if test='username != null'>username = #{username},</if>",
            "<if test='password != null'>password = #{password},</if>", // 密码在 Service 层处理
            "<if test='email != null'>email = #{email},</if>",
            "<if test='realName != null'>real_name = #{realName},</if>",
            "<if test='avatar != null'>avatar = #{avatar},</if>",
            // 通常不应在此处更新 status 和 ban_end_time，除非有特定逻辑
            "<if test='status != null'>status = #{status},</if>",
            "<if test='banEndTime != null'>ban_end_time = #{banEndTime},</if>",
            "update_time = NOW()", // 总是更新修改时间
            "</set>",
            "WHERE id = #{id} AND is_deleted = 0", // 添加条件
            "</script>"
    })
    int updateByIdSelective(User user);

    /**
     * 注销用户（软删除），将 is_deleted 设为 1
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET is_deleted = 1, update_time = NOW(), status = 2 " + // 可以同时更新 status 为注销状态，例如 2
            "WHERE id = #{userId} AND is_deleted = 0") // 只注销未注销的账号
    int deactivateUserById(@Param("userId") Long userId);

    /**
     * （可选）根据ID重新激活用户
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET is_deleted = 0, status = 0, update_time = NOW() WHERE id = #{userId} AND is_deleted = 1") // 只激活已注销的
    int reactivateUserById(@Param("userId") Long userId);

    /**
     * 【新增】根据邮箱查询用户，包括已被软删除的记录
     * @param email 邮箱地址
     * @return 用户对象，如果不存在则返回 null
     */
    @Select("SELECT id, user_code, username, password, email, avatar, real_name, status, ban_end_time, create_time, update_time, is_deleted " +
            "FROM sys_user WHERE email = #{email}") // 不包含 is_deleted 条件
    User selectByEmailIncludingDeleted(@Param("email") String email);

    /**
     * 【修改】计算满足条件的【纯粹】普通用户总数 (roleId=9 且没有其他角色)
     *
     * @param searchQuery  搜索关键字 (可能为 null 或空字符串)
     * @param filterStatus 状态过滤 (可能为 null)
     * @return 满足条件的总记录数
     */
    @Select("""
            <script>
            SELECT COUNT(DISTINCT u.id) /* 使用 COUNT(DISTINCT u.id) 避免因多角色关联导致计数重复 */
            FROM
                sys_user u
            INNER JOIN
                sys_user_role ur ON u.id = ur.user_id
            WHERE u.is_deleted = 0 AND ur.role_id = 9 /* 必须有普通用户角色 */
              /* 并且：该用户不存在任何非普通用户的角色记录 */
              AND NOT EXISTS (
                  SELECT 1
                  FROM sys_user_role ur_admin
                  WHERE ur_admin.user_id = u.id AND ur_admin.role_id != 9
              )
                <if test='filterStatus != null'>
                    AND u.status = #{filterStatus}
                </if>
                <if test='searchQuery != null and searchQuery != ""'>
                    AND (u.user_code LIKE CONCAT('%', #{searchQuery}, '%')
                         OR u.real_name LIKE CONCAT('%', #{searchQuery}, '%')
                         OR u.email LIKE CONCAT('%', #{searchQuery}, '%')
                         OR u.username LIKE CONCAT('%', #{searchQuery}, '%'))
                </if>
            </script>
            """)
    long countUsersByPage( // 返回 long 类型
                           @Param("searchQuery") String searchQuery,
                           @Param("filterStatus") Integer filterStatus
    );


    /**
     * 【修改】分页、条件查询【纯粹普通用户】列表 (roleId=9 且没有其他角色)
     *
     * @param searchQuery  搜索关键字
     * @param filterStatus 状态过滤
     * @param sortBy       排序字段
     * @param sortOrder    排序顺序
     * @param offset       分页偏移量
     * @param limit        每页数量
     * @return 当前页的用户列表
     */
    @Select("""
            <script>
            SELECT /* DISTINCT 确保即使 join 出多行也只返回一个用户 */
                DISTINCT u.id, u.user_code, u.username, u.email, u.avatar, u.real_name,
                u.status, u.ban_end_time, u.create_time, u.update_time, u.is_deleted
            FROM
                sys_user u
            INNER JOIN
                sys_user_role ur ON u.id = ur.user_id
            WHERE u.is_deleted = 0 AND ur.role_id = 9 /* 必须有普通用户角色 */
              /* 并且：该用户不存在任何非普通用户的角色记录 */
              AND NOT EXISTS (
                  SELECT 1
                  FROM sys_user_role ur_admin
                  WHERE ur_admin.user_id = u.id AND ur_admin.role_id != 9
              )
                <if test='filterStatus != null'>
                    AND u.status = #{filterStatus}
                </if>
                <if test='searchQuery != null and searchQuery != ""'>
                    AND (u.user_code LIKE CONCAT('%', #{searchQuery}, '%')
                         OR u.real_name LIKE CONCAT('%', #{searchQuery}, '%')
                         OR u.email LIKE CONCAT('%', #{searchQuery}, '%')
                         OR u.username LIKE CONCAT('%', #{searchQuery}, '%'))
                </if>
            <choose>
                <when test='sortBy != null and sortBy == "name"'>
                    ORDER BY u.real_name ${sortOrder}
                </when>
                <when test='sortBy != null and sortBy == "code"'>
                    ORDER BY u.user_code ${sortOrder}
                </when>
                <when test='sortBy != null and sortBy == "username"'>
                    ORDER BY u.username ${sortOrder}
                </when>
                <otherwise>
                    /* 默认按创建时间降序 */
                    ORDER BY u.create_time DESC
                </otherwise>
            </choose>
            LIMIT #{offset}, #{limit} /* 手动添加 LIMIT 子句 */
            </script>
            """)
    List<User> findUsersManuallyPaginated(
            @Param("searchQuery") String searchQuery,
            @Param("filterStatus") Integer filterStatus,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder,
            @Param("offset") long offset,
            @Param("limit") int limit
    );
}