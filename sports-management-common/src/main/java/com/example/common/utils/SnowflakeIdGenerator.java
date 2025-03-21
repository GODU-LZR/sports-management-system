package com.example.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * 雪花算法生成唯一ID工具类
 */
@Component
public class SnowflakeIdGenerator {
    // 起始时间戳（2023-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1672531200000L;

    // 机器ID所占的位数
    private static final long MACHINE_ID_BITS = 5L;

    // 数据中心ID所占的位数
    private static final long DATA_CENTER_ID_BITS = 5L;

    // 序列号所占的位数
    private static final long SEQUENCE_BITS = 12L;

    // 机器ID的最大值
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);

    // 数据中心ID的最大值
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    // 序列号的最大值
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 机器ID向左移的位数
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;

    // 数据中心ID向左移的位数
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;

    // 时间戳向左移的位数
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATA_CENTER_ID_BITS;

    // 数据中心ID
    private long dataCenterId;

    // 机器ID
    private long machineId;

    // 序列号
    private long sequence = 0L;

    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     */
    public SnowflakeIdGenerator() {
        // 通过构造函数或者注入的方式初始化 ID
    }

    /**
     * 初始化时使用配置文件中的值
     */
    @PostConstruct
    public void init() {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("Data center ID can't be greater than " + MAX_DATA_CENTER_ID + " or less than 0");
        }
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException("Machine ID can't be greater than " + MAX_MACHINE_ID + " or less than 0");
        }
    }

    /**
     * 生成下一个唯一ID
     *
     * @return 唯一ID
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();

        // 如果当前时间小于上次生成ID的时间，说明系统时钟回退，抛出异常
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        // 如果是同一毫秒内生成的，则递增序列号
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 如果序列号超过最大值，则等待到下一毫秒
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            // 如果时间戳改变，则重置序列号
            sequence = 0L;
        }

        // 更新上次生成ID的时间戳
        lastTimestamp = currentTimestamp;

        // 生成ID并返回
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }

    /**
     * 等待到下一毫秒
     *
     * @param currentTimestamp 当前时间戳
     * @return 下一毫秒的时间戳
     */
    private long waitNextMillis(long currentTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= currentTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();
        for (int i = 0; i < 10; i++) {
            System.out.println("Generated ID: " + idGenerator.nextId());
        }
    }

    // 通过 @Value 注解从配置文件注入值
    @Value("${snowflake.dataCenterId:1}") // 默认值为 1
    public void setDataCenterId(long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    @Value("${snowflake.machineId:1}") // 默认值为 1
    public void setMachineId(long machineId) {
        this.machineId = machineId;
    }
}
