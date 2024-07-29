package com.wz.xlinksnap.common.constant;


/**
 * Redis常量类
 */
public interface RedisConstant {


    /**
     * 长链缓存key
     */
    public static final String LONG_URL_KEY = "lurl-key:";

    /**
     * 创建短链分布式锁
     */
    public static final String CREATE_SURL_LOCK = "create-surl-lock:";

    /**
     * 指标key
     */
    public static final String DAILY_METRICS = "daily-metrics:";

    /**
     * 获取指标key
     */
    public static String getMetricsKey(String surl, String metricType) {
        return DAILY_METRICS + surl + ":" + metricType;
    }
}
