package com.example.common.lock.aop;

import com.example.common.lock.DistributedLock;
import com.example.common.lock.anno.RedisDistributedLockable;
import com.example.common.lock.impl.redis.RedisLockHandle;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.expression.ExpressionParser;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


/**
 * @Description: "分布式锁实现的aop,方法实现前后自动拿锁的释放"
 * @Param:
 * @return:
 * @Author: ZX_Sun
 * @Date: 2025-05-12
 */
@Aspect
@Component
@Slf4j
public class DistributedLockAspect {
    @Value("${inventory.lock.wait-time:5}")
    private long waitTime;

    @Value("${inventory.lock.lease-time:10}")
    private long leaseTime;
    private final DistributedLock distributedLock;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Autowired
    public DistributedLockAspect(DistributedLock distributedLock, ParameterNameDiscoverer parameterNameDiscoverer) {
        this.distributedLock = distributedLock;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }
    @Around("@annotation(redisDistributedLockable)")
    public Object aroundLockableMethod(ProceedingJoinPoint joinPoint, RedisDistributedLockable redisDistributedLockable){
        RedisLockHandle lockHandle=null;
        String lockKey=null;
        try{
            MethodSignature signature=(MethodSignature) joinPoint.getSignature();
            Method method=signature.getMethod();
            Object[]args=joinPoint.getArgs();//拿参

//            构建上下文
            EvaluationContext context = new StandardEvaluationContext();

            String[]parameterNames=parameterNameDiscoverer.getParameterNames(method);
            if(parameterNames!=null &&parameterNames.length>0){
                for(int i=0;i<parameterNames.length;i++){
                    context.setVariable(parameterNames[i], args[i]); // 将参数名和参数值放入上下文
                }
            }
            context.setVariable("methodName", method.getName());
            context.setVariable("target", joinPoint.getTarget());
            context.setVariable("args", args); // 也可以通过 #args[index] 访问
            Expression expression = parser.parseExpression(redisDistributedLockable.resourcekey());
            lockKey = expression.getValue(context, String.class);
            if (lockKey == null || lockKey.isEmpty()) {
                log.error("Failed to generate lock key for method: {}. Lock will not be acquired.", method.getName());
                // 如果 key 生成失败，可以选择抛异常或直接执行方法
                return joinPoint.proceed();
            }
            // 2. 尝试获取锁
            long waitTimeMillis = redisDistributedLockable.waitTimeUnit().toMillis(redisDistributedLockable.waitTime());
            long leaseTimeMillis = redisDistributedLockable.leaseTimeUnit().toMillis(redisDistributedLockable.leaseTime());

            lockHandle = (RedisLockHandle) distributedLock.acquire(lockKey, waitTimeMillis, leaseTimeMillis, TimeUnit.SECONDS);


            // 3. 判断是否成功获取锁
            if (lockHandle == null) {
                log.warn("Failed to acquire lock for key: {} after waiting {}ms. Method will not be executed.", lockKey, waitTimeMillis);
                // TODO: 这里可以根据需求抛出自定义异常，或者执行一个降级/补偿逻辑
//                throw new LockAcquisitionException("Failed to acquire distributed lock for key: " + lockKey);
                  throw new RuntimeException("取锁失败:"+lockKey);
            }

            log.debug("Successfully acquired lock for key: {}", lockKey);

            // 4. 执行目标方法
            return joinPoint.proceed();

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        finally {
            if (lockHandle !=null){
                try{
                    distributedLock.release(lockHandle);
                    log.debug("Successfully released lock for key: {}", lockKey);
                } catch (Exception e) {
                    // 锁释放失败通常是严重问题，需要记录日志
                    log.error("Failed to release lock for key: {}", lockKey, e);
                    // 注意：这里通常不应该 re-throw 异常，以免影响业务方法的返回值或抛出的业务异常。
                }
            }
        }
    }
}
