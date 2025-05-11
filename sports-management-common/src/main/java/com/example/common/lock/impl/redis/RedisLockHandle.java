package com.example.common.lock.impl.redis;// src/main/java/com/example/common/lock/impl/RedisLockHandle.java


import com.example.common.lock.LockHandle;
import java.util.Objects;

public class RedisLockHandle implements LockHandle {
    private final String resourceKey;
    private final String uniqueValue; // 用于标识锁的持有者

    public RedisLockHandle(String resourceKey, String uniqueValue) {
        this.resourceKey = resourceKey;
        this.uniqueValue = uniqueValue;
    }

    @Override
    public String getResourceKey() {
        return resourceKey;
    }

    public String getUniqueValue() {
        return uniqueValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisLockHandle that = (RedisLockHandle) o;
        return Objects.equals(resourceKey, that.resourceKey) &&
                Objects.equals(uniqueValue, that.uniqueValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceKey, uniqueValue);
    }

    @Override
    public String toString() {
        return "RedisLockHandle{" +
                "resourceKey='" + resourceKey + '\'' +
                ", uniqueValue='" + uniqueValue + '\'' +
                '}';
    }
}
