package com.example.event.service;

import com.example.common.response.Result;
import com.example.event.pojo.Test;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "TestServer", description = "测试接口")
public interface TestServer {

    @Operation(summary = "创建测试数据")
    Result<Test> createTest(@Parameter(description = "测试实体") Test test);

    @Operation(summary = "根据ID删除测试数据")
    Result<Void> deleteTest(@Parameter(description = "测试数据ID") Long id);

    @Operation(summary = "更新测试数据")
    Result<Test> updateTest(@Parameter(description = "测试实体") Test test);

    @Operation(summary = "根据ID获取测试数据")
    Result<Test> getTestById(@Parameter(description = "测试数据ID") Long id);

    @Operation(summary = "获取所有测试数据")
    Result<List<Test>> getAllTests();
}
