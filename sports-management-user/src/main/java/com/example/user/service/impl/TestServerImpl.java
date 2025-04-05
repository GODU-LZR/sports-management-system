package com.example.user.service.impl;

import com.example.common.response.Result;
import com.example.user.mapper.TestMapper;
import com.example.user.pojo.Test;
import com.example.user.service.TestServer;
import com.example.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
public class TestServerImpl implements TestServer {

    @Autowired
    private TestMapper testMapper;

    @Autowired
    private RedisUtil redisUtil;

    private static final String TEST_CACHE_KEY_PREFIX = "test:";
    private static final String TEST_LOCK_KEY_PREFIX = "lock:test:";

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
}
