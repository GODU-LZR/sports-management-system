package com.example.equipment.EqOrder;

import com.example.common.lock.LockHandle;
import com.example.common.lock.impl.redis.RedisDistributedLockImpl;
import com.example.equipment.mapper.CategoryMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@DubboService(version = "1.0.0")
@Slf4j
public class EqOrderService {
    @Autowired
    private CategoryMapper categoryMapper;
    // 提炼锁键前缀为常量
    private static final String INVENTORY_REDUCE_LOCK_PREFIX = "inventory:reduce:";

    private final RedisDistributedLockImpl redisDistributedLock;

    @Value("${inventory.lock.wait-time:5}")
    private long waitTime;

    @Value("${inventory.lock.lease-time:10}")
    private long leaseTime;

    @Autowired
    public EqOrderService(RedisDistributedLockImpl redisDistributedLock) {
        this.redisDistributedLock = redisDistributedLock;
    }

    //减少实际库存
    public boolean reduceInventory(Long CategoryId, int quantity) {
        String lockKey = INVENTORY_REDUCE_LOCK_PREFIX + CategoryId; // 使用常量
        LockHandle lockHandle = null;
        try {
            lockHandle = redisDistributedLock.acquire(lockKey, waitTime, leaseTime, TimeUnit.SECONDS);
            if (lockHandle == null) {
                log.warn("在允许的时间内未能获取到物品库存减少的锁：{}", CategoryId);
                return false;
            }
            log.info("成功获取物品锁：{}，开始减少库存数量：{}", CategoryId, quantity);

            boolean reductionSuccessful = performInventoryReduction(CategoryId, quantity);

            if (reductionSuccessful) {
                log.info("成功减少物品库存：{}，减少数量：{}", CategoryId, quantity);
                return true;
            } else {
                log.warn("库存减少失败，物品库存不足：{}，请求减少数量：{}", CategoryId, quantity);
                return false;
            }

        } catch (Exception e) {
            log.error("物品库存减少过程中发生错误：{}", CategoryId, e);
            return false;
        } finally {
            if (lockHandle != null) {
                boolean released = redisDistributedLock.release(lockHandle);
                if (!released) {
                    log.warn("释放物品锁失败：{}，锁句柄：{}", CategoryId, lockHandle);
                }
            }
        }
    }

    private boolean performInventoryReduction(Long categoryId, int quantity) {

//        categoryMapper.reduceBookStockWithNums(categoryId, quantity);

        return true;

        // 5. 如果库存不足，返回 false 表示扣减失败

    }


}