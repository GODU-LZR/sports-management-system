package com.example.ai.controller;

import com.example.ai.pojo.Test;
import com.example.ai.service.TestServer;
import com.example.common.response.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
@Tag(name = "TestController", description = "测试接口控制器")
public class TestController {

    @Autowired
    private TestServer testServer;

    @PostMapping
    @Operation(summary = "创建测试数据")
    public Result<Test> createTest(@RequestBody Test test) {
        return testServer.createTest(test);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除测试数据")
    public Result<Void> deleteTest(@PathVariable Long id) {
        return testServer.deleteTest(id);
    }

    @PutMapping
    @Operation(summary = "更新测试数据")
    public Result<Test> updateTest(@RequestBody Test test) {
        return testServer.updateTest(test);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取测试数据")
    public Result<Test> getTestById(@PathVariable Long id) {
        return testServer.getTestById(id);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有测试数据")
    public Result<List<Test>> getAllTests() {
        return testServer.getAllTests();
    }
}
