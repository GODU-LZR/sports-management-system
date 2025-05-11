package com.example.common.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {
    /**
     * 尝试获取指定资源的分布式锁。
     * 如果在等待时间内成功获取锁，则返回一个 LockHandle 对象；
     * 否则返回 null。
     *
     * @param resource 锁定的资源唯一标识符 (例如 "equipment:stock:101")
     * @param waitTime    等待获取锁的最长时间
     * @param leaseTime   如果成功获取锁，锁的有效租期（自动过期时间）
     * @param unit        waitTime 和 leaseTime 的时间单位
     * @return 成功获取锁返回 LockHandle，否则返回 null
     */
    LockHandle acquire(String resource , long waitTime, long leaseTime, TimeUnit unit);
    /**
     * 释放之前通过 acquire() 获取的锁。
     * 必须传入获取锁时返回的 LockHandle 对象。
     *
     * @param handle 需要释放的锁句柄
     * @return 成功释放返回 true，否则返回 false (例如锁已过期或不是当前持有者)
     */
    boolean release(LockHandle handle);
    // 可选：非阻塞尝试获取锁
    // LockHandle tryAcquire(String resourceKey, long leaseTime, TimeUnit unit);


}
