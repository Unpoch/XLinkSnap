package com.wz.xlinksnap.common.constant;

/**
 * 消息枚举类
 */
public interface MessageConstant {

    /**
     * 消息的类型：邮件还是短信
     */
    public static final String SEND_TYPE_EMAIL = "0";

    public static final String SEND_TYPE_PHONE = "1";

    /**
     * 发送消息的要求
     */
    public static final String SEND_IN_TIME = "0";//立即发送

    public static final String SEND_IN_SCHEDULE = "1";//定时发送

}
