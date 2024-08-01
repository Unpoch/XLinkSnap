package com.wz.xlinksnap.common.constant;


/**
 * Redis常量类
 */
public interface RedisConstant {


    /**
     * 长链缓存key
     * 后缀为 lurl经过64编码后的字符串
     */
    public static final String LONG_URL_KEY = "lurl-key:";

    /**
     * 创建短链分布式锁
     * 后缀为 lurl经过64编码后的字符串
     */
    public static final String CREATE_SURL_LOCK = "create-surl-lock:";

    /**
     * 指标key
     * 后缀为 短链的后缀suffix
     */
    public static final String DAILY_METRICS = "daily-metrics:";

    /**
     * 用户验证码
     * 后缀为 邮箱或者手机号
     */
    public static final String USER_VERIFY_CODE = "user-verify-code:";

    /**
     * 获取指标key（短链后缀suffix，将唯一id Base62编码生成）
     */
    public static String getMetricsKey(String suffix, String metricType) {
        return DAILY_METRICS + suffix + ":" + metricType;
    }
}
