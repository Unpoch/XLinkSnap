package com.wz.xlinksnap.common.aspect;

import com.wz.xlinksnap.common.annotation.DistributedRLock;
import com.wz.xlinksnap.common.annotation.DistributedReadWriteLock;
import com.wz.xlinksnap.common.exception.ConditionException;
import com.wz.xlinksnap.model.dto.req.BatchCreateShortUrlReq;
import com.wz.xlinksnap.model.dto.req.CreateShortUrlReq;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Redisson分布式锁切面类
 */
@Aspect
@Order(1)
@Component
@Slf4j
public class RedissonLockAspect {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 定义RLock切点
     */
    @Pointcut("@annotation(com.wz.xlinksnap.common.annotation.DistributedRLock)")
    public void rLockPointcut() {
    }


    /**
     * 定义RReadWriteLock切点
     */
    @Pointcut("@annotation(com.wz.xlinksnap.common.annotation.DistributedReadWriteLock)")
    public void rReadWriteLockPointcut() {
    }


    @Around("rReadWriteLockPointcut() && @annotation(distributedReadWriteLock)")
    public Object rReadWriteLockAround(ProceedingJoinPoint joinPoint,
                                       DistributedReadWriteLock distributedReadWriteLock) throws Throwable {
        String lockKey = distributedReadWriteLock.prefix();
        RReadWriteLock rwLock = redissonClient.getReadWriteLock(lockKey);
        long waitTime = distributedReadWriteLock.waitTime();
        long leaseTime = distributedReadWriteLock.leaseTime();
        DistributedReadWriteLock.LockType lockType = distributedReadWriteLock.lockType();
        RLock lock = null;
        boolean acquired = false;
        if (lockType == DistributedReadWriteLock.LockType.READ) {
            lock = rwLock.readLock();
        } else if (lockType == DistributedReadWriteLock.LockType.WRITE) {
            lock = rwLock.writeLock();
        }
        if (lock != null) {
            acquired = tryLock(lock, waitTime, leaseTime);
        }
        try {
            if (acquired) {
                return joinPoint.proceed();
            } else {
                throw new RuntimeException("Could not acquire lock");
            }
        } finally {
            if (acquired && lock != null) {
                lock.unlock();
            }
        }
    }


    /**
     * 注解@RLock 环绕通知
     * rlockPointcut() 指定切入点，选择@RLock注解的方法作为连接点
     * rLock 是切面方法 rlockAroud 的一个参数，用于接收被注解的方法上的 @RLock 注解实例。通过这个参数，
     * 我们可以访问注解的属性值，如锁的名称、等待时间、租约时间
     */
    @Around("rLockPointcut() && @annotation(distributedRLock)")
    public Object rLockAround(ProceedingJoinPoint joinPoint,
                              DistributedRLock distributedRLock) throws Throwable {
        String suffix = getLockKeySuffix(joinPoint);
        //获取注解参数
        String lockKey = distributedRLock.prefix() + suffix;//拼接lockKey
        long waitTime = distributedRLock.waitTime();
        long leaseTime = distributedRLock.leaseTime();
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = tryLock(lock, waitTime, leaseTime);
            if (acquired) {
                return joinPoint.proceed();
            } else {
                throw new ConditionException("获取分布式锁失败");
            }
        } finally {
            log.info(lockKey, acquired, lock);
            if (acquired) {
                lock.unlock();
            }
        }
    }

    /**
     * 尝试获取分布式锁
     * @param lock
     * @param waitTime
     * @param leaseTime
     * @return
     * @throws InterruptedException
     */
    private boolean tryLock(RLock lock, long waitTime, long leaseTime) throws InterruptedException {
        if (waitTime == -1 && leaseTime == -1) {
            lock.lock();
            return true;
        } else if (waitTime == -1) {
            lock.lock(leaseTime, TimeUnit.MILLISECONDS);
            return true;
        } else {
            return lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 获取分布式锁key的后缀
     * @param joinPoint
     * @return
     */
    private String getLockKeySuffix(ProceedingJoinPoint joinPoint) {
        //获取注解所在方法参数
        Object[] args = joinPoint.getArgs();
        String suffix = "";//key的后缀
        for (Object arg : args) {
            if (arg instanceof CreateShortUrlReq) {//如果是短链创建请求
                CreateShortUrlReq createShortUrlReq = (CreateShortUrlReq) arg;
                suffix = createShortUrlReq.getLurl();
                break;
            }else if(arg instanceof BatchCreateShortUrlReq) {
                BatchCreateShortUrlReq batchCreateShortUrlReq = (BatchCreateShortUrlReq) arg;
                suffix = batchCreateShortUrlReq.getDomain();//TODO：这里暂时用域名
                break;
            }
        }
        return suffix;
    }
}