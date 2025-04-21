package com.example.user.service.impl;

import com.example.common.response.Result;
import com.example.common.response.ResultCode;
import com.example.common.services.VerificationCodeService;
import com.example.user.mapper.TestMapper;
import com.example.user.pojo.Test;
import com.example.user.service.TestServer;
import com.example.common.utils.RedisUtil;
// 引入 Dubbo 的 @Reference 注解
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Reference;

import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service // Spring 的 @Service
public class TestServerImpl implements TestServer {

    private static final Logger log = LoggerFactory.getLogger(TestServerImpl.class); // 添加 Logger

    @Autowired
    private TestMapper testMapper;

    @Autowired
    private RedisUtil redisUtil;

    // 使用 @Reference 注入 Dubbo 服务消费者代理
    // check=false 表示启动时不检查提供者是否存在，避免启动强依赖
    @DubboReference(version = "1.0.0", check = false)
    private VerificationCodeService verificationCodeService;

    private static final String TEST_CACHE_KEY_PREFIX = "test:";
    private static final String TEST_LOCK_KEY_PREFIX = "lock:test:";

    // ... 原有的 createTest, deleteTest, updateTest, getTestById, getAllTests 方法保持不变 ...
    @Override
    public Result<Test> createTest(Test test) {
        // 插入数据库
        testMapper.insert(test);
        // 更新缓存
        String key = TEST_CACHE_KEY_PREFIX + test.getId();
        redisUtil.set(key, test, 3600); // 缓存1小时
        return Result.success(test);
    }

    @Override
    public Result<Void> deleteTest(Long id) {
        // 删除数据库记录
        testMapper.deleteById(id);
        // 删除缓存
        String key = TEST_CACHE_KEY_PREFIX + id;
        redisUtil.delete(key);
        return Result.success();
    }

    @Override
    public Result<Test> updateTest(Test test) {
        String lockKey = TEST_LOCK_KEY_PREFIX + test.getId();
        String requestId = UUID.randomUUID().toString();

        try {
            // 尝试获取分布式锁，超时时间为30秒
            boolean locked = redisUtil.tryLock(lockKey, requestId, 30, TimeUnit.SECONDS);
            if (!locked) {
                return Result.error("系统繁忙，请稍后重试");
            }

            // 执行更新操作
            testMapper.updateById(test);
            // 更新缓存
            String cacheKey = TEST_CACHE_KEY_PREFIX + test.getId();
            redisUtil.set(cacheKey, test, 3600);
            return Result.success(test);
        } finally {
            redisUtil.releaseLock(lockKey, requestId);
        }
    }

    @Override
    public Result<Test> getTestById(Long id) {
        String key = TEST_CACHE_KEY_PREFIX + id;
        // 先从缓存获取
        Object cachedData = redisUtil.get(key);
        if (cachedData != null) {
            // FastJson2 会自动处理类型转换
            if (cachedData instanceof Test) {
                return Result.success((Test) cachedData);
            }
        }

        // 缓存未命中，从数据库获取
        Test test = testMapper.selectById(id);
        if (test != null) {
            redisUtil.set(key, test, 3600); // 缓存1小时
        }
        return Result.success(test);
    }

    @Override
    public Result<List<Test>> getAllTests() {
        // 从数据库获取所有数据
        List<Test> tests = testMapper.selectList(null);
        return Result.success(tests);
    }

    @Override
    public Result<Boolean> sendVerificationEmail(String email) {
        log.info("TestServer: Received request to send verification code to {}", email);
        if (verificationCodeService == null) {
            log.error("VerificationCodeService (Dubbo Reference) is null. Check Dubbo configuration and provider status.");
            // 注意：你提供的 Result 类中没有 error(boolean, String) 的方法，
            // 应该使用 error(Integer, String) 或 error(String) 或 error(IResultCode)
            // return Result.error(false,"验证码服务不可用"); // 编译会失败
            return Result.error(ResultCode.ERROR.getCode(), "验证码服务不可用 (Dubbo reference is null)"); // 使用 code + message
        }
        try {
            // 调用 Dubbo 远程服务
            boolean success = verificationCodeService.sendCode(email);
            if (success) {
                log.info("Successfully called verificationCodeService.sendCode for email {}", email);
                // 注意：Result.success(T data, String message) 似乎也不是你 Result 类支持的构造方式
                // 应该使用 success(T data) 或 success()
                // return Result.success(true, "验证码发送任务已启动"); // 可能编译失败
                return Result.success(true); // 返回成功状态和数据 true
            } else {
                log.warn("Call to verificationCodeService.sendCode for email {} returned false", email);
                // return Result.error(false, "启动验证码发送任务失败"); // 编译会失败
                return Result.error(ResultCode.ERROR.getCode(), "中间件服务未能成功启动发送任务");
            }
        } catch (RpcException e) {
            log.error("Dubbo RpcException when calling verificationCodeService.sendCode for email {}", email, e);
            // return Result.error(false, "调用验证码服务时出错: " + e.getMessage()); // 编译会失败
            return Result.error(ResultCode.ERROR.getCode(), "调用验证码服务时出错: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected exception when calling verificationCodeService.sendCode for email {}", email, e);
            // return Result.error(false, "发送验证码时发生未知错误"); // 编译会失败
            return Result.error(ResultCode.ERROR.getCode(), "发送验证码时发生未知错误");
        }
    }
}