package com.example.notification.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类，封装常用的Redis操作
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ========== Key操作 ==========

    /**
     * 设置过期时间
     * @param key 键
     * @param timeout 时间
     * @param unit 时间单位
     * @return true成功 false失败
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            if (timeout > 0) {
                redisTemplate.expire(key, timeout, unit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置过期时间（秒）
     * @param key 键
     * @param seconds 秒
     * @return true成功 false失败
     */
    public boolean expire(String key, long seconds) {
        return expire(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取过期时间
     * @param key 键
     * @return 时间(秒) 返回0代表永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     * @param key 键
     * @return true存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     * @param key 可以传一个或多个值
     */
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     * 删除缓存
     * @param keys 键集合
     */
    public void delete(Collection<String> keys) {
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 按前缀匹配删除Key
     * @param pattern 模式，如user:*
     */
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    // ========== String操作 ==========

    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置缓存并设置过期时间
     * @param key 键
     * @param value 值
     * @param timeout 时间
     * @param unit 时间单位
     * @return true成功 false失败
     */
    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (timeout > 0) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置缓存并设置过期时间（秒）
     * @param key 键
     * @param value 值
     * @param seconds 时间(秒) 注意:如果时间小于等于0 将设置无限期
     * @return true成功 false失败
     */
    public boolean set(String key, Object value, long seconds) {
        return set(key, value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 递增
     * @param key 键
     * @param delta 递增因子(必须大于0)
     * @return 递增后的值
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     * @param key 键
     * @param delta 递减因子(必须大于0)
     * @return 递减后的值
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ========== Hash操作 ==========

    /**
     * 获取hash项对应的值
     * @param key 键
     * @param field item
     * @return 值
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取所有hash项的值
     * @param key 键
     * @return 所有键值
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 设置hash的多个键值对
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hSetAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置hash的多个键值对，并设置过期时间
     * @param key 键
     * @param map 对应多个键值
     * @param seconds 时间(秒)
     * @return true 成功 false 失败
     */
    public boolean hSetAll(String key, Map<String, Object> map, long seconds) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (seconds > 0) {
                expire(key, seconds);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向hash表中放入一个数据项
     * @param key 键
     * @param field 项
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向hash表中放入一个数据项，并设置过期时间
     * @param key 键
     * @param field 项
     * @param value 值
     * @param seconds 时间(秒)
     * @return true 成功 false 失败
     */
    public boolean hSet(String key, String field, Object value, long seconds) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            if (seconds > 0) {
                expire(key, seconds);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的字段
     * @param key 键
     * @param fields 字段
     */
    public void hDelete(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断hash表中是否有该字段
     * @param key 键
     * @param field 字段
     * @return true 存在 false 不存在
     */
    public boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * hash表递增
     * @param key 键
     * @param field 字段
     * @param delta 递增因子
     * @return 递增后的值
     */
    public double hIncr(String key, String field, double delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    /**
     * hash表递减
     * @param key 键
     * @param field 字段
     * @param delta 递减因子
     * @return 递减后的值
     */
    public double hDecr(String key, String field, double delta) {
        return redisTemplate.opsForHash().increment(key, field, -delta);
    }

    // ========== Set操作 ==========

    /**
     * 获取Set中的所有值
     * @param key 键
     * @return 所有值
     */
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断Set中是否存在value
     * @param key 键
     * @param value 值
     * @return true 存在 false 不存在
     */
    public boolean sIsMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将数据放入Set
     * @param key 键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将数据放入Set并设置过期时间
     * @param key 键
     * @param seconds 时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sAdd(String key, long seconds, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (seconds > 0) {
                expire(key, seconds);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取Set的大小
     * @param key 键
     * @return 大小
     */
    public long sSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除Set中的值
     * @param key 键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long sRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ========== List操作 ==========

    /**
     * 获取List指定范围内的元素
     * @param key 键
     * @param start 开始
     * @param end 结束  0 到 -1代表所有值
     * @return 元素列表
     */
    public List<Object> lRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取List长度
     * @param key 键
     * @return 长度
     */
    public long lSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过索引获取List中的元素
     * @param key 键
     * @param index 索引 index>=0时，0表头，1第二个元素，依次类推；index<0时，-1表尾，-2倒数第二个元素，依次类推
     * @return 元素
     */
    public Object lIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 向List右侧添加元素
     * @param key 键
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean rPush(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向List右侧添加元素并设置过期时间
     * @param key 键
     * @param value 值
     * @param seconds 时间(秒)
     * @return true 成功 false 失败
     */
    public boolean rPush(String key, Object value, long seconds) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (seconds > 0) {
                expire(key, seconds);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向List右侧批量添加元素
     * @param key 键
     * @param values 值列表
     * @return true 成功 false 失败
     */
    public boolean rPushAll(String key, List<Object> values) {
        try {
            redisTemplate.opsForList().rightPushAll(key, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向List右侧批量添加元素并设置过期时间
     * @param key 键
     * @param values 值列表
     * @param seconds 时间(秒)
     * @return true 成功 false 失败
     */
    public boolean rPushAll(String key, List<Object> values, long seconds) {
        try {
            redisTemplate.opsForList().rightPushAll(key, values);
            if (seconds > 0) {
                expire(key, seconds);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向List左侧添加元素
     * @param key 键
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean lPush(String key, Object value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除List中N个值为value的元素
     * @param key 键
     * @param count 移除多少个，0表示所有
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ========== 分布式锁操作 ==========

    /**
     * 获取分布式锁
     * @param lockKey 锁的Key
     * @param requestId 请求标识
     * @param expireTime 超期时间
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String requestId, long expireTime, TimeUnit timeUnit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, requestId, expireTime, timeUnit));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁的Key
     * @param requestId 请求标识，获取锁时的requestId
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String requestId) {
        try {
            // 使用lua脚本保证原子性：判断requestId是否一致，一致则删除锁
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long result = redisTemplate.execute(
                    RedisScript.of(script, Long.class),
                    Collections.singletonList(lockKey),
                    requestId);
            return result != null && result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}