package com.wz.xlinksnap.common.concurrent;

/**
 * 并发处理接口，用于支持集合入参
 */
public interface SupplierHandler<T, P> {

    T handle(P param);
}