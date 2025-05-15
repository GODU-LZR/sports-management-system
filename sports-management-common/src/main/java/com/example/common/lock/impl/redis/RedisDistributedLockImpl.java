package com.example.common.lock.impl.redis;

import com.example.common.lock.DistributedLock;
import com.example.common.lock.LockHandle;
import com.example.common.utils.RedisUtil;
import com.example.common.utils.SnowflakeIdGenerator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
public class RedisDistributedLockImpl implements DistributedLock {

    // Spring Data Redis 会自动配置 RedisTemplate，这里使用 <String, String> 适配锁的 key 和 value
    private final RedisTemplate<String, String> redisTemplate;
   @Autowired
    public RedisDistributedLockImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    @Autowired
    SnowflakeIdGenerator snowflakeIdGenerator;
    private static final String RELEASE_LOCK_LUA_SCRIPT_STRING=
            "if redis.call('get',KEYS[1]) == ARGV[1] then " +
                    " return redis.call('del',KEYS[1]) " +
                    "else " +
                    " return 0 " +
                    "end";
    private static final RedisScript<Long>RELEASE_LOCK_LUA_SCRIPT=
            new DefaultRedisScript<>(RELEASE_LOCK_LUA_SCRIPT_STRING);

    private static final long DEFAULT_ACQUIRE_INIERVAL_MS = 50;



    @Override
    public LockHandle acquire(String resourceKey, long waitTime, long leaseTime, TimeUnit unit) {
        if (resourceKey == null || resourceKey.isEmpty() || leaseTime <= 0 || unit == null) {
            log.warn("Invalid parameters for acquire lock: resourceKey={}, waitTime={}, leaseTime={}, unit={}",
                    resourceKey, waitTime, leaseTime, unit);
            return null;
        }
        String uniqueValue = String.valueOf(snowflakeIdGenerator.nextId());

        long startTime = System.currentTimeMillis();//多主从下的时间差考虑

        long waitTimeMillis = unit.toMillis(waitTime);
        long leaseTimeMillis = unit.toMillis(leaseTime);
        while (true) {
          try{  // 使用 setIfAbsent(key, value, timeout, unit) 尝试获取锁
            // 底层使用 SET key value NX PX milliseconds 命令
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                    resourceKey,
                    uniqueValue,
                    Duration.ofMillis(leaseTimeMillis)
            );
            // setIfAbsent 返回 true 表示设置成功 (即获取到锁)
            if (Boolean.TRUE.equals(acquired)) {
                log.debug("Successfully acquired lock for resource: {}", resourceKey);
                return new RedisLockHandle(resourceKey, uniqueValue);
            }
            // 如果设置失败 (key 已存在)，且等待时间已到，则放弃
            if (System.currentTimeMillis() - startTime >= waitTimeMillis) {
                log.debug("Failed to acquire lock for resource {} within {} {}", resourceKey, waitTime, unit);
                return null; // 超时，未能获取锁
            }
            long sleepTime= ThreadLocalRandom.current().nextLong(DEFAULT_ACQUIRE_INIERVAL_MS/2,DEFAULT_ACQUIRE_INIERVAL_MS);
//            log.trace("Lock for {} not acquired, waiting for {}ms before retrying", resourceKey, sleepTime);
            Thread.sleep(sleepTime);
          }catch (InterruptedException e){
              Thread.currentThread().interrupt();
              return null;
          }catch (DataAccessException e){
              // Redis 操作异常，例如连接问题
              log.error("Redis DataAccessException during lock acquisition for resource: {}", resourceKey, e);
              // 可以选择重试、返回null或抛出自定义异常
              // 这里简单返回null，实际应用可能需要更复杂的错误处理
              return null;
          }catch (Exception e){
              // 其他未知异常
              log.error("Unexpected error during lock acquisition for resource: {}", resourceKey, e);
              return null;
          }
        }
    }
/**
* 根据得到的lockhandle释放锁
 *
 * **/
    @Override
    public boolean release(LockHandle handle) {
        if(!(handle instanceof RedisLockHandle)){
            log.warn("尝试释放的是一个非redis的锁: {}", handle);
            return false;
        }
        RedisLockHandle redisLockHandle=(RedisLockHandle) handle;
        String resourceKey=redisLockHandle.getResourceKey();
        String uniqueValue=redisLockHandle.getUniqueValue();
        if (resourceKey == null || uniqueValue == null) {
            log.warn("非法数据: resourceKey={}, uniqueValue={}", resourceKey, uniqueValue);
            return false;
        }
        try {
            List<String>keys= Collections.singletonList(resourceKey);
            Object result = redisTemplate.execute(
                    RELEASE_LOCK_LUA_SCRIPT,
                    keys,
                    uniqueValue
            );
            // Lua 脚本返回 1 表示成功删除，返回 0 表示 key 不存在或 value 不匹配
            boolean released = result != null && (Long) result == 1L;
//            if (released) {
//                log.debug("Successfully released lock for resource: {}", resourceKey);
//            } else {
//                log.warn("Failed to release lock for resource {}. It might have expired or been released by another process. Handle: {}", resourceKey, handle);
//            }
            return released;

        } catch (DataAccessException e) {
            // Redis 操作异常
            log.error("redis连接数据异常: {}", resourceKey, e);
            // 释放失败通常是严重问题，需要关注
            return false;
        } catch (Exception e) {
            // 其他未知异常
            log.error("未知错误: {}", resourceKey, e);
            return false;
        }
    }
}
