package com.wz.xlinksnap.common.util;

/**
 * URL工具类
 */
public class UrlUtil {

    public static final String HTTPS = "https:";
    public static final String HTTP = "http:";

    /**
     * 构建短链返回
     *
     * @param domain
     * @param suffix
     * @return
     */
    public static String buildShortUrl(String domain, String suffix) {
        return HTTPS + "//" + domain + "//" + suffix;
    }
}
