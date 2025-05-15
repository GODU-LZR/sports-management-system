package com.example.common.lock.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisDistributedLockable {
    /**
     * 支持 Spring Expression Language (SpEL)，例如：
     * "#args[0]" 表示取第一个方法参数作为 key
     * "#user.id" 表示取第一个参数（假设是 User 对象）的 id 属性作为 key
     * "'order:' + #orderId" 表示拼接字符串和参数
     * 如果需要使用参数名，请确保你的项目开启了参数名编译选项 (Java 8+ 默认支持，或使用 -parameters 编译标志)。
     */
    String resourcekey();
    /**
     * 获取锁的等待时间。如果在指定时间内没有获取到锁，则放弃。
     * 默认值：0L (立即失败)
     */
    /**
     * 获取锁的等待时间。如果在指定时间内没有获取到锁，则放弃。
     * 默认值：0L (立即失败)
     * 获取锁的等待时间单位。默认值：TimeUnit.SECONDS
     */
    long waitTime() default 0L;
    TimeUnit waitTimeUnit() default TimeUnit.SECONDS;

    /**
     * 锁的持有时间（租约时间）。如果持有锁的线程在该时间内没有完成业务逻辑，锁会自动释放。
     * 建议结合看门狗使用，看门狗会在锁即将过期前自动续期。
     * 默认值：30L (秒)
     * 锁的持有时间单位。默认值：TimeUnit.SECONDS
     */
    long leaseTime() default 30L;
    TimeUnit leaseTimeUnit() default TimeUnit.SECONDS;
}
