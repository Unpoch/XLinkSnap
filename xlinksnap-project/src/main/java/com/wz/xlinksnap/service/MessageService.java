package com.wz.xlinksnap.service;

/**
 * 发送消息服务接口
 */
public interface MessageService {
    void sendCodeByEmail(String email);

    void sendCodeByPhone(String phone);

    boolean verifyCode(String s,String code);

    void sendMessageByEmail(String subject,String msgBody, String email);

    void sendMessageByPhone(String subject,String msgBody, String phone);
}
