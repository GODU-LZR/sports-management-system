package com.example.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.event.dao.GameRoleRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper // Spring 或 MyBatis 的注解，标记这是一个 Mapper 接口
public interface GameRoleRecordMapper extends BaseMapper<GameRoleRecord> {


}
