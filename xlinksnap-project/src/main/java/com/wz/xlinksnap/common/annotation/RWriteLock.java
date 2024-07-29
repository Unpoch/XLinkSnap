package com.wz.xlinksnap.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁写锁实现
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RWriteLock {
    String value();
    long waitTime() default -1;
    long leaseTime() default -1;
}