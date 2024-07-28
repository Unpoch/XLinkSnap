package com.wz.xlinksnap.common.util;

/**
 * 10-62进制转换器
 */
public class Base62Converter {

    private static final String BASE62_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    /**
     * 10进制转62进制
     */
    public static String encode(long number) {
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(BASE62_CHARACTERS.charAt((int) (number % BASE)));
            number /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * 62进制转10进制
     */
    public static long decode(String base62) {
        long result = 0;
        for (char character : base62.toCharArray()) {
            result = result * BASE + BASE62_CHARACTERS.indexOf(character);
        }
        return result;
    }
}