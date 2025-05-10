package com.example.finance.controller;

import com.example.common.response.Result;
import com.example.finance.pojo.Test;
import com.example.finance.service.TestServer;
import com.example.finance.service.impl.TestServerImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
@Tag(name = "TestController", description = "测试接口控制器")
public class TestController {
    @DubboReference(version = "1.0.0", check = false,retries = 0)
    TestServer testServer;


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
