package com.example.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.event.dao.GameRoleRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper // Spring 或 MyBatis 的注解，标记这是一个 Mapper 接口
public interface GameRoleRecordMapper extends BaseMapper<GameRoleRecord> {

    // 你可以在这里定义自定义的查询方法，例如：
    // List<GameRoleRecord> getRecordsByUserId(Long userId);
    // List<GameRoleRecord> getRecordsByGameId(Long gameId);
    // GameRoleRecord getRecordByUserIdAndGameId(Long userId, Long gameId);

    // 注意：MyBatis-Plus 默认会处理枚举到数据库值的转换，
    // 但如果需要更精细的控制，或者数据库字段类型与枚举 code 类型不完全匹配，
    // 可能需要配置 TypeHandler。不过对于 INT 类型的 code，通常不需要额外配置。
}
