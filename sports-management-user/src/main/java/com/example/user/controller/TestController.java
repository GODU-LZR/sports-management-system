package com.example.user.controller;

import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.user.pojo.Test;
import com.example.user.service.TestServer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
@Tag(name = "TestController", description = "测试接口控制器")
public class TestController {


    @Autowired
    @Qualifier("testServerImpl")
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
    public Result<List<Test>> getAllTests(@Parameter(hidden = true) UserConstant currentUser) {
        System.out.println(currentUser);
        return testServer.getAllTests();
    }

    @PostMapping("/sendVerificationCode")
    @Operation(summary = "发送邮箱验证码 (通过 Dubbo)", description = "调用中间件服务发送验证码")
    @ApiResponse(responseCode = "200", description = "返回是否成功调用服务以及相关信息")
    public Result<Boolean> triggerSendCode(
            @Parameter(description = "目标邮箱地址", required = true, example = "test@example.com")
            @RequestParam String email) {
        return testServer.sendVerificationEmail(email);
    }

}
