package com.example.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.notification.pojo.Test;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TestMapper extends BaseMapper<Test> {

    @Select("SELECT * FROM test WHERE username = #{username}")
    Test selectByUsername(@Param("username") String username);

    // 示例：使用注解自定义更新语句
    @Update("UPDATE test SET password = #{password} WHERE id = #{id}")
    int updatePasswordById(@Param("id") Long id, @Param("password") String password);

    // 示例：使用注解自定义删除语句 (逻辑删除，如果你的表有 is_deleted 字段)
    @Update("UPDATE test SET is_deleted = 1 WHERE id = #{id}")
    int logicDeleteById(@Param("id") Long id);

    // 示例：使用注解自定义插入语句 (如果需要更复杂的插入逻辑)
    @Insert("INSERT INTO test (username, password, email) VALUES (#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id") // 获取自增主键
    int customInsert(Test test);
}
