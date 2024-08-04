package com.wz.xlinksnap.common.util;

import java.util.Arrays;
import java.util.Base64;

/**
 * Base64编码工具类
 */
//64 个字符（A-Z, a-z, 0-9, +, /）
public class Base64Converter {

    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();


    /**
     * 将输入字符串转换为Base64编码
     */
    public static String encodeToBase64(String input) {
        // 将字符串转换为字节数组
        byte[] bytes = input.getBytes();
        // 进行 Base64 编码
        return encoder.encodeToString(bytes);
    }

    /**
     * 将Base64编码的字符串转换为原字符串
     */
    public static String decodeToOriginString(String base64Str) {
        return Arrays.toString(decoder.decode(base64Str));
    }
}
