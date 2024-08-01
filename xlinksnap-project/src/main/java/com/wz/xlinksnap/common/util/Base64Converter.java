package com.wz.xlinksnap.common.util;

import java.util.Base64;

/**
 * Base64编码
 */
//64 个字符（A-Z, a-z, 0-9, +, /）
public class Base64Converter {

    /**
     * Base64编码
     */
    public static String encodeToBase64(String input) {
        // 将字符串转换为字节数组
        byte[] bytes = input.getBytes();
        // 进行 Base64 编码
        return Base64.getEncoder().encodeToString(bytes);
    }
}
