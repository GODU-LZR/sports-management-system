package com.example.middleware.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 雪花ID生成器
 * 雪花ID结构：1位符号位 + 41位时间戳 + 10位工作机器ID + 12位序列号
 */
@Component
public class SnowflakeIdGeneratorM {
    // 开始时间截 (2023-01-01)
    private static final long twepoch = 1672531200000L;
    
    // 机器ID所占的位数
    private static final long workerIdBits = 5L;
    // 数据标识ID所占的位数
    private static final long datacenterIdBits = 5L;
    
    // 支持的最大机器ID，结果是31
    private static final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    // 支持的最大数据标识ID，结果是31
    private static final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    
    // 序列在ID中占的位数
    private static final long sequenceBits = 12L;
    
    // 机器ID向左移12位
    private static final long workerIdShift = sequenceBits;
    // 数据标识ID向左移17位(12+5)
    private static final long datacenterIdShift = sequenceBits + workerIdBits;
    // 时间截向左移22位(5+5+12)
    private static final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    
    // 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
    private static final long sequenceMask = -1L ^ (-1L << sequenceBits);
    
    // 工作机器ID(0~31)
    private static long workerId;
    // 数据中心ID(0~31)
    private static long datacenterId;
    // 毫秒内序列(0~4095)
    private static long sequence = 0L;
    // 上次生成ID的时间截
    private static long lastTimestamp = -1L;
    
    @Value("${snowflake.dataCenterId}")
    private long dataCenterId;
    
    @Value("${snowflake.machineId}")
    private long machineId;
    
    /**
     * 初始化静态变量
     */
    @PostConstruct
    public void init() {
        if (machineId > maxWorkerId || machineId < 0) {
            throw new IllegalArgumentException(String.format("Worker ID can't be greater than %d or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxDatacenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("Datacenter ID can't be greater than %d or less than 0", maxDatacenterId));
        }
        workerId = machineId;
        datacenterId = dataCenterId;
    }
    
    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    public static synchronized long nextId() {
        long timestamp = timeGen();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }
        
        // 上次生成ID的时间截
        lastTimestamp = timestamp;
        
        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }
    
    /**
     * 获取字符串格式的ID
     * @return 字符串ID
     */
    public static String nextIdStr() {
        return String.valueOf(nextId());
    }
    
    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
    
    /**
     * 返回以毫秒为单位的当前时间
     * @return 当前时间(毫秒)
     */
    protected static long timeGen() {
        return System.currentTimeMillis();
    }
}