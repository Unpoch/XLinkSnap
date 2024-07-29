package com.wz.xlinksnap.common.util;

import com.wz.xlinksnap.common.exception.ConditionException;

/**
 * URL工具类
 */
public class UrlUtil {

    public static final String HTTPS = "https:";
    public static final String HTTP = "http:";

    /**
     * 构建短链返回
     */
    public static String buildShortUrl(String domain, String suffix) {
        return HTTPS + "//" + domain + "/" + suffix;
    }

    /**
     * 获取短链的suffix
     */
    public static String getShortUrlSuffix(String surl) {
        int lastIndex = surl.lastIndexOf("/");
        if (lastIndex == -1) throw new ConditionException("短链存在问题！");
        return surl.substring(lastIndex + 1);
    }
}
