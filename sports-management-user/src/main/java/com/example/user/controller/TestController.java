package com.example.user.controller;

import com.alibaba.dubbo.rpc.RpcContext;
import com.example.common.model.Result;
import com.example.user.pojo.Test;
import com.example.user.service.TestServer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/test")
@Tag(name = "TestController", description = "测试接口控制器")
public class TestController {


    @Autowired
    @Qualifier("testServerImpl")
    private TestServer testServer;

    // 远程调用 venue 模块的 TestServer 接口
    @DubboReference
    @Qualifier("dubbotestServer")
    private TestServer dubbotestServer;

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


    /**
     * 新增 dubbo 测试方法
     * 调用 venue 模块的 /test/all 接口，并将返回结果打印到控制台。
     * 注意：这里需要通过 RpcContext 设置 HTTP Basic 认证信息，
     *      其中账号为 gatewayuser，密码为 gatewaypass（该密码应与 venue 模块的配置一致）。
     */
    @GetMapping("/dubboTest")
    @Operation(summary = "Dubbo 测试调用 venue 模块的 /test/all 接口")
    public String dubboTest() {
        // 设置 HTTP Basic 认证信息：账号 gatewayuser，密码 gatewaypass
        String systemUsername = "gatewayuser";
        String systemPassword = "gatewaypass";
        String authString = systemUsername + ":" + systemPassword;
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
        // 将认证头添加到 Dubbo 的上下文附件中，供远程服务接收
        RpcContext.getContext().setAttachment("Authorization", basicAuth);

        // 远程调用 venue 模块的 /test/all 接口
        Result<List<Test>> result = dubbotestServer.getAllTests();

        // 将结果打印到控制台
        System.out.println("Dubbo Test Result: " + result);

        return "Dubbo Test Completed. Check console for result.";
    }
}
