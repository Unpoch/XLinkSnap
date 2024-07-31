package com.wz.xlinksnap.service.impl;

import com.wz.xlinksnap.common.constant.RedisConstant;
import com.wz.xlinksnap.common.exception.ConditionException;
import com.wz.xlinksnap.service.MessageService;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailSender;
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

    /**
     * 发送邮箱验证码
     */
    @Override
    public void sendCodeByEmail(String email) {
        String code = generateCode();
        //有效期：五分钟
        redisTemplate.opsForValue().set(RedisConstant.USER_VERIFY_CODE + email,code,5, TimeUnit.MINUTES);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
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
    public boolean verifyCode(String s,String code) {
        String sendCode = redisTemplate.opsForValue().get(RedisConstant.USER_VERIFY_CODE + s);
        if(StringUtils.isEmpty(sendCode)) {
            throw new ConditionException("验证码已过期！请重新发送");
        }
        return code.equals(sendCode);
    }

    /*
     * 生成六位验证码
     */
    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
