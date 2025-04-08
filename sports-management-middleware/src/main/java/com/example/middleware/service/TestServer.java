package com.example.middleware.service;

import com.example.common.response.Result;

import com.example.middleware.pojo.TestCLASS1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "TestServer", description = "测试接口")
public interface TestServer {

    @Operation(summary = "创建测试数据")
    Result<TestCLASS1> createTest(@Parameter(description = "测试实体") TestCLASS1 test);

    @Operation(summary = "根据ID删除测试数据")
    Result<Void> deleteTest(@Parameter(description = "测试数据ID") Long id);

    @Operation(summary = "更新测试数据")
    Result<TestCLASS1> updateTest(@Parameter(description = "测试实体") TestCLASS1 test);

    @Operation(summary = "根据ID获取测试数据")
    Result<TestCLASS1> getTestById(@Parameter(description = "测试数据ID") Long id);

    @Operation(summary = "获取所有测试数据")
    Result<List<TestCLASS1>> getAllTests();
}
