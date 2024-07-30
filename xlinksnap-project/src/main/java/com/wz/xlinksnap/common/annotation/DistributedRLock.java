package com.wz.xlinksnap.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁可重入锁注解实现
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedRLock {

    String prefix();
    long waitTime() default -1;
    long leaseTime() default -1;
}