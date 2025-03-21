package com.example.user.config;

import com.alibaba.fastjson2.support.spring6.data.redis.FastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.password}")c
    private String password;

    @Value("${redis.database:0}")
    private int database;

    @Value("${redis.timeout:3000}")
    private int timeout;

    @Value("${redis.lettuce.pool.max-active:8}")
    private int maxActive;

    @Value("${redis.lettuce.pool.max-wait:-1}")
    private long maxWait;

    @Value("${redis.lettuce.pool.max-idle:8}")
    private int maxIdle;

    @Value("${redis.lettuce.pool.min-idle:0}")
    private int minIdle;

    @Value("${redis.key-prefix:sportsys:}")
    private String keyPrefix;

    /**
     * Redis连接工厂配置
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis服务器配置
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        redisConfig.setPassword(RedisPassword.of(password));
        redisConfig.setDatabase(database);

        // 连接池配置
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWait);  // 使用 setMaxWaitMillis 方法


        // Lettuce客户端配置
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout))
                .poolConfig(poolConfig)
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    /**
     * RedisTemplate配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 FastJson2JsonRedisSerializer 来序列化和反序列化 Redis 的 value 值

        FastJsonRedisSerializer<Object> fastJson2JsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);

        // 使用 StringRedisSerializer 来序列化和反序列化 Redis 的 key 值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key 采用 String 的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // value 采用 FastJson2 的序列化方式
        template.setValueSerializer(fastJson2JsonRedisSerializer);
        // hash key 采用 String 的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // hash value 采用 FastJson2 的序列化方式
        template.setHashValueSerializer(fastJson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }


    /**
     * 缓存管理器配置
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith(keyPrefix)
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new FastJsonRedisSerializer<>(Object.class)))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}