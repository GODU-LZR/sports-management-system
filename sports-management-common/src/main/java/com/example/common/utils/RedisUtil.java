package com.example.common.utils; // 确认包名是否正确

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.slf4j.Logger; // 引入 SLF4J Logger
import org.slf4j.LoggerFactory; // 引入 SLF4J LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils; // 确认导入

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类，封装常用的Redis操作
 */
@Component
public class RedisUtil {

    // 使用 SLF4J 进行日志记录
    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ========== Key操作 ==========

    /**
     * 设置过期时间
     * @param key 键
     * @param timeout 时间
     * @param unit 时间单位
     * @return true成功 false失败 (基于 redisTemplate.expire 的返回值)
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            if (key != null && timeout > 0 && unit != null) {
                // redisTemplate.expire 返回 Boolean
                Boolean result = redisTemplate.expire(key, timeout, unit);
                log.debug("设置 Key '{}' 过期时间: {} {}, 结果: {}", key, timeout, unit, result);
                return Boolean.TRUE.equals(result); // 处理 null 情况
            }
            return false; // 无效参数
        } catch (Exception e) {
            log.error("设置 Key '{}' 过期时间失败", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间（秒）
     * @param key 键
     * @param seconds 秒 (大于0)
     * @return true成功 false失败
     */
    public boolean expire(String key, long seconds) {
        return expire(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取过期时间
     * @param key 键
     * @return 时间(秒) 返回-2代表key不存在, 返回-1代表永久有效，其他为剩余秒数
     */
    public long getExpire(String key) {
        try {
            if (key == null) return -2; // 或抛异常
            // getExpire 返回 Long
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            log.debug("获取 Key '{}' 过期时间: {}", key, expire);
            return expire != null ? expire : -2; // 如果 key 不存在，getExpire 可能返回 null 或 -2，这里统一返回 -2
        } catch (Exception e) {
            log.error("获取 Key '{}' 过期时间失败", key, e);
            return -2; // 出错时也认为 key 无效或无法获取
        }
    }

    /**
     * 判断key是否存在
     * @param key 键
     * @return true存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            if (key == null) return false;
            // hasKey 返回 Boolean
            Boolean result = redisTemplate.hasKey(key);
            log.debug("检查 Key '{}' 是否存在: {}", key, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("检查 Key '{}' 是否存在失败", key, e);
            return false;
        }
    }

    /**
     * 删除缓存 (void 方法，尽力而为)
     * @param keys 键集合
     */
    public void delete(Collection<String> keys) {
        if (!CollectionUtils.isEmpty(keys)) {
            try {
                // delete 返回 Long (删除的数量)
                Long deletedCount = redisTemplate.delete(keys);
                log.debug("删除 Keys: {}, 数量: {}", keys, deletedCount);
            } catch (Exception e) {
                log.error("删除 Keys {} 失败", keys, e);
            }
        }
    }

    /**
     * 删除单个缓存 (新增方法，返回 boolean)
     * @param key 键
     * @return true 如果 key 被删除, false 如果 key 不存在或删除失败
     */
    public boolean delete(String key) {
        if (key == null) return false;
        try {
            // delete(key) 返回 Boolean
            Boolean deleted = redisTemplate.delete(key);
            log.debug("删除 Key '{}', 结果: {}", key, deleted);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("删除 Key '{}' 失败", key, e);
            return false;
        }
    }


    /**
     * 按前缀匹配删除Key (void 方法，尽力而为)
     * 注意: keys 命令在生产环境大数据量下慎用，可能阻塞 Redis
     * @param pattern 模式，如user:*
     */
    public void deleteByPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) return;
        try {
            // keys 返回 Set<String>
            Set<String> keys = redisTemplate.keys(pattern);
            if (!CollectionUtils.isEmpty(keys)) {
                log.warn("即将通过模式 '{}' 删除 Keys: {}", pattern, keys); // 记录一下，因为 keys 操作有风险
                // delete 返回 Long
                Long deletedCount = redisTemplate.delete(keys);
                log.debug("通过模式 '{}' 删除 Keys 数量: {}", pattern, deletedCount);
            } else {
                log.debug("模式 '{}' 未匹配到任何 Key", pattern);
            }
        } catch (Exception e) {
            log.error("通过模式 '{}' 删除 Keys 失败", pattern, e);
        }
    }

    // ========== String操作 ==========

    /**
     * 获取缓存
     * @param key 键
     * @return 值 (Object)
     */
    public Object get(String key) {
        try {
            if (key == null) return null;
            // opsForValue().get() 返回 Object
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("获取 Key '{}', Value 类型: {}", key, (value != null ? value.getClass().getSimpleName() : "null"));
            return value;
        } catch (Exception e) {
            log.error("获取 Key '{}' 失败", key, e);
            return null;
        }
    }


    /**
     * 获取缓存并直接反序列化为指定类型的对象 (使用 FastJSON2)
     *
     * @param key   键
     * @param clazz 目标类型的 Class 对象
     * @param <T>   目标类型
     * @return 反序列化后的对象，如果 key 不存在、值不是有效的 JSON 或反序列化失败，则返回 null
     */
    public <T> T getFromJson(String key, Class<T> clazz) {
        try {
            if (key == null || clazz == null) {
                log.warn("getFromJson 调用参数无效: key={}, clazz={}", key, clazz);
                return null;
            }
            // 1. 获取原始数据
            Object cachedData = get(key); // 调用内部的 get 方法，包含日志

            // 2. 检查是否为空 (缓存未命中)
            if (cachedData == null) {
                // get 方法已经记录了 debug 日志，这里无需重复记录 cache miss
                return null;
            }

            // 3. 尝试将获取到的数据转换为 JSON 字符串，然后解析
            // FastJSON2 的 toJSONString 可以处理 String, JSONObject, Map 等多种输入
            String jsonString = JSON.toJSONString(cachedData);
            // 检查转换后的字符串是否有效
            if (jsonString == null || jsonString.isEmpty() || "null".equalsIgnoreCase(jsonString.trim())) {
                log.warn("getFromJson - Data for key '{}' converted to empty or null JSON string. Original type: {}", key, cachedData.getClass().getName());
                return null;
            }

            // 4. 解析 JSON 字符串
            T result = JSON.parseObject(jsonString, clazz);
            log.debug("getFromJson - Successfully deserialized key '{}' to type {}", key, clazz.getSimpleName());
            return result;

        } catch (JSONException jsonEx) { // 捕获 FastJSON 解析异常
            // 尝试获取原始数据用于日志记录，如果失败则记录 null
            Object rawDataForLog = null;
            try { rawDataForLog = get(key); } catch (Exception ignored) {}
            log.error("getFromJson - Failed to parse JSON for key '{}' into type {}. Invalid JSON format? Raw data sample: {}",
                    key, clazz.getSimpleName(), truncate(JSON.toJSONString(rawDataForLog), 200), jsonEx); // 记录截断后的原始数据
            return null;
        } catch (Exception e) { // 捕获其他潜在异常
            log.error("getFromJson - Failed to get or deserialize key '{}' into type {}", key, clazz.getSimpleName(), e);
            return null;
        }
    }

    // 辅助方法：截断字符串用于日志
    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }


    /**
     * 设置缓存 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            if (key == null) return false;
            // opsForValue().set() 是 void 方法
            redisTemplate.opsForValue().set(key, value);
            log.debug("设置 Key '{}' 成功", key);
            return true;
        } catch (Exception e) {
            log.error("设置 Key '{}' 失败", key, e);
            return false;
        }
    }

    /**
     * 设置缓存并设置过期时间 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param value 值
     * @param timeout 时间 (必须大于0)
     * @param unit 时间单位
     * @return true成功 false失败
     */
    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (key == null || unit == null) return false;
            if (timeout > 0) {
                // opsForValue().set(key, value, timeout, unit) 是 void 方法
                redisTemplate.opsForValue().set(key, value, timeout, unit);
                log.debug("设置 Key '{}' 并设置过期时间: {} {} 成功", key, timeout, unit);
            } else {
                // 如果 timeout <= 0，则调用普通 set
                set(key, value);
                log.debug("设置 Key '{}' (无过期时间) 成功", key);
            }
            return true;
        } catch (Exception e) {
            log.error("设置 Key '{}' 并设置过期时间失败", key, e);
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
     * @return 递增后的值 (如果 key 不存在，会先初始化为 0 再递增)
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            log.error("递增因子必须大于0, key: {}", key);
            throw new IllegalArgumentException("递增因子必须大于0"); // 抛出更明确的异常
        }
        try {
            if (key == null) throw new IllegalArgumentException("Key 不能为空");
            // increment 返回 Long
            Long result = redisTemplate.opsForValue().increment(key, delta);
            log.debug("Key '{}' 递增 {}, 结果: {}", key, delta, result);
            return result != null ? result : 0L; // increment 不会返回 null，除非出错
        } catch (Exception e) {
            log.error("Key '{}' 递增失败", key, e);
            // 根据业务决定是抛出异常还是返回默认值
            // throw new RuntimeException("Redis 递增操作失败", e);
            return 0L; // 或者返回一个表示错误的值
        }
    }

    /**
     * 递减
     * @param key 键
     * @param delta 递减因子(必须大于0)
     * @return 递减后的值 (如果 key 不存在，会先初始化为 0 再递减)
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            log.error("递减因子必须大于0, key: {}", key);
            throw new IllegalArgumentException("递减因子必须大于0");
        }
        try {
            if (key == null) throw new IllegalArgumentException("Key 不能为空");
            // decrement 返回 Long
            Long result = redisTemplate.opsForValue().decrement(key, delta);
            log.debug("Key '{}' 递减 {}, 结果: {}", key, delta, result);
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.error("Key '{}' 递减失败", key, e);
            // throw new RuntimeException("Redis 递减操作失败", e);
            return 0L;
        }
    }

    // ========== Hash操作 ==========

    /**
     * 获取hash项对应的值
     * @param key 键
     * @param field item
     * @return 值 (Object)
     */
    public Object hGet(String key, String field) {
        try {
            if (key == null || field == null) return null;
            // opsForHash().get() 返回 Object
            Object value = redisTemplate.opsForHash().get(key, field);
            log.debug("HGET Key: '{}', Field: '{}', Value 类型: {}", key, field, (value != null ? value.getClass().getSimpleName() : "null"));
            return value;
        } catch (Exception e) {
            log.error("HGET Key: '{}', Field: '{}' 失败", key, field, e);
            return null;
        }
    }

    /**
     * 获取所有hash项的值
     * @param key 键
     * @return 所有键值 (Map<Object, Object>)
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            if (key == null) return Collections.emptyMap(); // 返回空 Map 而不是 null
            // entries 返回 Map<Object, Object>
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            log.debug("HGETALL Key: '{}', 获取到 {} 个条目", key, entries.size());
            return entries;
        } catch (Exception e) {
            log.error("HGETALL Key: '{}' 失败", key, e);
            return Collections.emptyMap(); // 出错时也返回空 Map
        }
    }

    /**
     * 设置hash的多个键值对 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hSetAll(String key, Map<String, Object> map) {
        try {
            if (key == null || map == null || map.isEmpty()) return false;
            // putAll 是 void 方法
            redisTemplate.opsForHash().putAll(key, map);
            log.debug("HSETALL Key: '{}', 设置 {} 个条目成功", key, map.size());
            return true;
        } catch (Exception e) {
            log.error("HSETALL Key: '{}' 失败", key, e);
            return false;
        }
    }

    /**
     * 设置hash的多个键值对，并设置过期时间 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param map 对应多个键值
     * @param seconds 时间(秒)
     * @return true 成功 false 失败
     */
    public boolean hSetAll(String key, Map<String, Object> map, long seconds) {
        try {
            if (key == null || map == null || map.isEmpty()) return false;
            // putAll 是 void 方法
            redisTemplate.opsForHash().putAll(key, map);
            log.debug("HSETALL Key: '{}', 设置 {} 个条目成功", key, map.size());
            if (seconds > 0) {
                // 调用我们自己的 expire 方法，它会处理结果并返回 boolean
                boolean expired = expire(key, seconds);
                if (!expired) {
                    log.warn("HSETALL Key: '{}' 后设置过期时间 {} 秒失败", key, seconds);
                    // 根据业务决定是否因为 expire 失败而整体返回 false
                    // return false;
                }
            }
            return true; // 即使 expire 失败，putAll 成功也可能算成功
        } catch (Exception e) {
            log.error("HSETALL Key: '{}' 并设置过期时间失败", key, e);
            return false;
        }
    }

    /**
     * 向hash表中放入一个数据项 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param field 项
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean hSet(String key, String field, Object value) {
        try {
            if (key == null || field == null) return false;
            // put 是 void 方法
            redisTemplate.opsForHash().put(key, field, value);
            log.debug("HSET Key: '{}', Field: '{}' 成功", key, field);
            return true;
        } catch (Exception e) {
            log.error("HSET Key: '{}', Field: '{}' 失败", key, field, e);
            return false;
        }
    }

    /**
     * 向hash表中放入一个数据项，并设置过期时间 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param field 项
     * @param value 值
     * @param seconds 时间(秒)
     * @return true 成功 false 失败
     */
    public boolean hSet(String key, String field, Object value, long seconds) {
        try {
            if (key == null || field == null) return false;
            // put 是 void 方法
            redisTemplate.opsForHash().put(key, field, value);
            log.debug("HSET Key: '{}', Field: '{}' 成功", key, field);
            if (seconds > 0) {
                if (!expire(key, seconds)) {
                    log.warn("HSET Key: '{}', Field: '{}' 后设置过期时间 {} 秒失败", key, field, seconds);
                    // return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("HSET Key: '{}', Field: '{}' 并设置过期时间失败", key, field, e);
            return false;
        }
    }

    /**
     * 删除hash表中的字段 (void 方法，尽力而为)
     * @param key 键
     * @param fields 字段 (一个或多个)
     */
    public void hDelete(String key, Object... fields) {
        if (key == null || fields == null || fields.length == 0) return;
        try {
            // delete 返回 Long (删除的数量)
            Long deletedCount = redisTemplate.opsForHash().delete(key, fields);
            log.debug("HDEL Key: '{}', Fields: {}, 数量: {}", key, Arrays.toString(fields), deletedCount);
        } catch (Exception e) {
            log.error("HDEL Key: '{}', Fields: {} 失败", key, Arrays.toString(fields), e);
        }
    }

    /**
     * 判断hash表中是否有该字段
     * @param key 键
     * @param field 字段
     * @return true 存在 false 不存在
     */
    public boolean hHasKey(String key, String field) {
        try {
            if (key == null || field == null) return false;
            // hasKey 返回 boolean
            Boolean result = redisTemplate.opsForHash().hasKey(key, field);
            log.debug("HEXISTS Key: '{}', Field: '{}', 结果: {}", key, field, result);
            // 注意：hasKey 直接返回 boolean，不是 Boolean，无需用 TRUE.equals
            return result != null && result; // 确保 result 不为 null (虽然理论上不会)
        } catch (Exception e) {
            log.error("HEXISTS Key: '{}', Field: '{}' 失败", key, field, e);
            return false;
        }
    }

    /**
     * hash表递增
     * @param key 键
     * @param field 字段
     * @param delta 递增因子 (可以为负数)
     * @return 递增/递减后的值
     */
    public double hIncr(String key, String field, double delta) {
        try {
            if (key == null || field == null) throw new IllegalArgumentException("Key 和 Field 不能为空");
            // increment 返回 Double
            Double result = redisTemplate.opsForHash().increment(key, field, delta);
            log.debug("HINCRBYFLOAT Key: '{}', Field: '{}', Delta: {}, 结果: {}", key, field, delta, result);
            return result != null ? result : 0.0;
        } catch (Exception e) {
            log.error("HINCRBYFLOAT Key: '{}', Field: '{}' 失败", key, field, e);
            // throw new RuntimeException("Redis Hash 递增操作失败", e);
            return 0.0;
        }
    }

    /**
     * hash表递减 (通过调用 hIncr 实现)
     * @param key 键
     * @param field 字段
     * @param delta 递减因子 (必须为正数)
     * @return 递减后的值
     */
    public double hDecr(String key, String field, double delta) {
        if (delta < 0) {
            log.error("Hash 递减因子必须大于等于0, key: {}, field: {}", key, field);
            throw new IllegalArgumentException("递减因子必须大于等于0");
        }
        return hIncr(key, field, -delta); // 调用 hIncr 传入负数
    }

    // ========== Set操作 ==========

    /**
     * 获取Set中的所有值
     * @param key 键
     * @return 所有值 (Set<Object>)
     */
    public Set<Object> sMembers(String key) {
        try {
            if (key == null) return Collections.emptySet();
            // members 返回 Set<Object>
            Set<Object> members = redisTemplate.opsForSet().members(key);
            log.debug("SMEMBERS Key: '{}', 获取到 {} 个成员", key, (members != null ? members.size() : 0));
            return members != null ? members : Collections.emptySet();
        } catch (Exception e) {
            log.error("SMEMBERS Key: '{}' 失败", key, e);
            return Collections.emptySet();
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
            if (key == null || value == null) return false;
            // isMember 返回 Boolean
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            log.debug("SISMEMBER Key: '{}', Value: {}, 结果: {}", key, value, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("SISMEMBER Key: '{}', Value: {} 失败", key, value, e);
            return false;
        }
    }

    /**
     * 将数据放入Set
     * @param key 键
     * @param values 值 可以是多个
     * @return 成功添加的个数 (long)
     */
    public long sAdd(String key, Object... values) {
        try {
            if (key == null || values == null || values.length == 0) return 0L;
            // add 返回 Long
            Long count = redisTemplate.opsForSet().add(key, values);
            log.debug("SADD Key: '{}', Values: {}, 成功个数: {}", key, Arrays.toString(values), count);
            return count != null ? count : 0L; // 处理 null 情况
        } catch (Exception e) {
            log.error("SADD Key: '{}', Values: {} 失败", key, Arrays.toString(values), e);
            return 0L;
        }
    }

    /**
     * 将数据放入Set并设置过期时间
     * @param key 键
     * @param seconds 时间(秒)
     * @param values 值 可以是多个
     * @return 成功添加的个数 (long)
     */
    public long sAdd(String key, long seconds, Object... values) {
        try {
            if (key == null || values == null || values.length == 0) return 0L;
            // add 返回 Long
            Long count = redisTemplate.opsForSet().add(key, values);
            long addedCount = (count != null ? count : 0L);
            log.debug("SADD Key: '{}', Values: {}, 成功个数: {}", key, Arrays.toString(values), addedCount);
            if (addedCount > 0 && seconds > 0) { // 只有成功添加了才设置过期
                if (!expire(key, seconds)) {
                    log.warn("SADD Key: '{}' 后设置过期时间 {} 秒失败", key, seconds);
                }
            }
            return addedCount;
        } catch (Exception e) {
            log.error("SADD Key: '{}', Values: {} 并设置过期时间失败", key, Arrays.toString(values), e);
            return 0L;
        }
    }

    /**
     * 获取Set的大小
     * @param key 键
     * @return 大小 (long)
     */
    public long sSize(String key) {
        try {
            if (key == null) return 0L;
            // size 返回 Long
            Long size = redisTemplate.opsForSet().size(key);
            log.debug("SCARD Key: '{}', 大小: {}", key, size);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.error("SCARD Key: '{}' 失败", key, e);
            return 0L;
        }
    }

    /**
     * 移除Set中的值
     * @param key 键
     * @param values 值 可以是多个
     * @return 成功移除的个数 (long)
     */
    public long sRemove(String key, Object... values) {
        try {
            if (key == null || values == null || values.length == 0) return 0L;
            // remove 返回 Long
            Long count = redisTemplate.opsForSet().remove(key, values);
            log.debug("SREM Key: '{}', Values: {}, 移除个数: {}", key, Arrays.toString(values), count);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("SREM Key: '{}', Values: {} 失败", key, Arrays.toString(values), e);
            return 0L;
        }
    }

    // ========== List操作 ==========

    /**
     * 获取List指定范围内的元素
     * @param key 键
     * @param start 开始 (0表示第一个)
     * @param end 结束 (负数表示倒数, -1表示最后一个) 0 到 -1代表所有值
     * @return 元素列表 (List<Object>)
     */
    public List<Object> lRange(String key, long start, long end) {
        try {
            if (key == null) return Collections.emptyList();
            // range 返回 List<Object>
            List<Object> range = redisTemplate.opsForList().range(key, start, end);
            log.debug("LRANGE Key: '{}', Start: {}, End: {}, 获取到 {} 个元素", key, start, end, (range != null ? range.size() : 0));
            return range != null ? range : Collections.emptyList();
        } catch (Exception e) {
            log.error("LRANGE Key: '{}', Start: {}, End: {} 失败", key, start, end, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取List长度
     * @param key 键
     * @return 长度 (long)
     */
    public long lSize(String key) {
        try {
            if (key == null) return 0L;
            // size 返回 Long
            Long size = redisTemplate.opsForList().size(key);
            log.debug("LLEN Key: '{}', 长度: {}", key, size);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.error("LLEN Key: '{}' 失败", key, e);
            return 0L;
        }
    }

    /**
     * 通过索引获取List中的元素
     * @param key 键
     * @param index 索引 index>=0时，0表头；index<0时，-1表尾
     * @return 元素 (Object)
     */
    public Object lIndex(String key, long index) {
        try {
            if (key == null) return null;
            // index 返回 Object
            Object value = redisTemplate.opsForList().index(key, index);
            log.debug("LINDEX Key: '{}', Index: {}, Value 类型: {}", key, index, (value != null ? value.getClass().getSimpleName() : "null"));
            return value;
        } catch (Exception e) {
            log.error("LINDEX Key: '{}', Index: {} 失败", key, index, e);
            return null;
        }
    }

    /**
     * 向List右侧添加元素 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean rPush(String key, Object value) {
        try {
            if (key == null) return false;
            // rightPush 返回 Long (列表长度)
            redisTemplate.opsForList().rightPush(key, value);
            log.debug("RPUSH Key: '{}', Value: {} 成功", key, value);
            return true; // 只要不抛异常就认为成功
        } catch (Exception e) {
            log.error("RPUSH Key: '{}', Value: {} 失败", key, value, e);
            return false;
        }
    }

    /**
     * 向List右侧添加元素并设置过期时间 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param value 值
     * @param seconds 时间(秒)
     * @return true 成功 false 失败
     */
    public boolean rPush(String key, Object value, long seconds) {
        try {
            if (key == null) return false;
            // rightPush 返回 Long
            redisTemplate.opsForList().rightPush(key, value);
            log.debug("RPUSH Key: '{}', Value: {} 成功", key, value);
            if (seconds > 0) {
                if (!expire(key, seconds)) {
                    log.warn("RPUSH Key: '{}' 后设置过期时间 {} 秒失败", key, seconds);
                    // return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("RPUSH Key: '{}', Value: {} 并设置过期时间失败", key, value, e);
            return false;
        }
    }

    /**
     * 向List右侧批量添加元素 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param values 值列表
     * @return true 成功 false 失败
     */
    public boolean rPushAll(String key, List<Object> values) {
        try {
            if (key == null || CollectionUtils.isEmpty(values)) return false;
            // rightPushAll 返回 Long
            redisTemplate.opsForList().rightPushAll(key, values);
            log.debug("RPUSH Key: '{}', 添加 {} 个 Values 成功", key, values.size());
            return true;
        } catch (Exception e) {
            log.error("RPUSH Key: '{}', 批量添加 Values 失败", key, e);
            return false;
        }
    }

    /**
     * 向List右侧批量添加元素并设置过期时间 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param values 值列表
     * @param seconds 时间(秒)
     * @return true 成功 false 失败
     */
    public boolean rPushAll(String key, List<Object> values, long seconds) {
        try {
            if (key == null || CollectionUtils.isEmpty(values)) return false;
            // rightPushAll 返回 Long
            redisTemplate.opsForList().rightPushAll(key, values);
            log.debug("RPUSH Key: '{}', 添加 {} 个 Values 成功", key, values.size());
            if (seconds > 0) {
                if (!expire(key, seconds)) {
                    log.warn("RPUSH Key: '{}' 批量添加后设置过期时间 {} 秒失败", key, seconds);
                    // return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("RPUSH Key: '{}', 批量添加 Values 并设置过期时间失败", key, e);
            return false;
        }
    }

    /**
     * 向List左侧添加元素 (操作成功返回 true，失败返回 false)
     * @param key 键
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean lPush(String key, Object value) {
        try {
            if (key == null) return false;
            // leftPush 返回 Long
            redisTemplate.opsForList().leftPush(key, value);
            log.debug("LPUSH Key: '{}', Value: {} 成功", key, value);
            return true;
        } catch (Exception e) {
            log.error("LPUSH Key: '{}', Value: {} 失败", key, value, e);
            return false;
        }
    }

    /**
     * 移除List中N个值为value的元素
     * @param key 键
     * @param count 移除多少个: >0 从表头开始找, <0 从表尾开始找, =0 移除所有匹配的
     * @param value 值
     * @return 成功移除的个数 (long)
     */
    public long lRemove(String key, long count, Object value) {
        try {
            if (key == null || value == null) return 0L;
            // remove 返回 Long
            Long removedCount = redisTemplate.opsForList().remove(key, count, value);
            log.debug("LREM Key: '{}', Count: {}, Value: {}, 移除个数: {}", key, count, value, removedCount);
            return removedCount != null ? removedCount : 0L;
        } catch (Exception e) {
            log.error("LREM Key: '{}', Count: {}, Value: {} 失败", key, count, value, e);
            return 0L;
        }
    }

    // ========== 分布式锁操作 ==========

    /**
     * 尝试获取分布式锁 (非阻塞)
     * @param lockKey 锁的Key
     * @param requestId 请求标识 (例如 UUID)，用于标识锁的持有者，防止误解锁
     * @param expireTime 超期时间
     * @param timeUnit 时间单位
     * @return true 获取成功, false 获取失败
     */
    public boolean tryLock(String lockKey, String requestId, long expireTime, TimeUnit timeUnit) {
        try {
            if (lockKey == null || requestId == null || timeUnit == null || expireTime <= 0) {
                log.warn("尝试获取锁参数无效: lockKey={}, requestId={}, expireTime={}, timeUnit={}", lockKey, requestId, expireTime, timeUnit);
                return false;
            }
            // setIfAbsent 返回 Boolean
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, requestId, expireTime, timeUnit);
            log.debug("尝试获取锁 Key: '{}', RequestId: {}, 过期: {} {}, 结果: {}", lockKey, requestId, expireTime, timeUnit, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("尝试获取锁 Key: '{}' 失败", lockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁 (使用 Lua 脚本保证原子性)
     * @param lockKey 锁的Key
     * @param requestId 请求标识，必须与获取锁时的 requestId 一致
     * @return true 释放成功, false 锁不存在或持有者不匹配导致释放失败
     */
    public boolean releaseLock(String lockKey, String requestId) {
        try {
            if (lockKey == null || requestId == null) {
                log.warn("尝试释放锁参数无效: lockKey={}, requestId={}", lockKey, requestId);
                return false;
            }
            // Lua 脚本: 原子地比较 requestId 并删除 key
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            // execute 返回脚本定义的类型，这里是 Long
            Long result = redisTemplate.execute(
                    RedisScript.of(script, Long.class), // 指定脚本和返回类型
                    Collections.singletonList(lockKey), // KEYS[1]
                    requestId);                         // ARGV[1]
            boolean released = (result != null && result > 0);
            log.debug("尝试释放锁 Key: '{}', RequestId: {}, Lua执行结果: {}, 释放结果: {}", lockKey, requestId, result, released);
            return released;
        } catch (Exception e) {
            log.error("释放锁 Key: '{}' 失败", lockKey, e);
            return false;
        }
    }
}
