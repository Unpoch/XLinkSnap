package com.wz.xlinksnap.service.impl;

import com.wz.xlinksnap.common.constant.RedisConstant;
import com.wz.xlinksnap.common.exception.ConditionException;
import com.wz.xlinksnap.service.MessageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 发送消息服务类
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * 发送邮箱验证码
     */
    @Override
    public void sendCodeByEmail(String email) {
        String code = generateCode();
        //有效期：五分钟
        redisTemplate.opsForValue().set(RedisConstant.USER_VERIFY_CODE + email, code, 5, TimeUnit.MINUTES);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(senderEmail);
        message.setSubject("XLinkSnap 验证码");
        message.setText("你的验证码是：" + code + "，有效期：五分钟");
        mailSender.send(message);
    }

    /**
     * TODO：发送手机验证码
     */
    @Override
    public void sendCodeByPhone(String phone) {

    }

    /**
     * 验证验证码是否正确
     */
    @Override
    public boolean verifyCode(String s, String code) {
        String sendCode = redisTemplate.opsForValue().get(RedisConstant.USER_VERIFY_CODE + s);
        if (StringUtils.isEmpty(sendCode)) {
            log.info("验证码已过期！发送的验证码：" + sendCode + "，输入的验证码：" + code + "，邮箱或者手机：" + s);
            throw new ConditionException("验证码已过期！请重新发送");
        }
        return code.equals(sendCode);
    }

    /**
     * 发送邮箱消息
     */
    @Override
    public void sendMessageByEmail(String subject, String msgBody, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);//标题
        message.setText(msgBody);//消息体
        message.setTo(email);
        message.setFrom(senderEmail);
        mailSender.send(message);
    }

    /**
     * TODO：待接入阿里云短信
     * 发送手机消息
     */
    @Override
    public void sendMessageByPhone(String subject, String msgBody, String phone) {

    }

    /**
     * 批量发送邮箱消息
     */
    @Override
    public void batchSendMessageByEmail(String subject, String text, List<String> emailList) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);//标题
        message.setText(text);//消息体
        message.setFrom(senderEmail);
        message.setTo(emailList.toArray(new String[0]));
        mailSender.send(message);
    }

    /**
     * TODO：批量发送短信消息
     */
    @Override
    public void batchSendMessageByPhone(String subject, String text, List<String> emailList) {

    }

    /*
     * 生成六位验证码
     */
    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
