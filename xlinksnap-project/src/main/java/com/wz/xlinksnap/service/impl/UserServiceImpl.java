package com.wz.xlinksnap.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wz.xlinksnap.common.exception.ConditionException;
import com.wz.xlinksnap.common.util.MD5Util;
import com.wz.xlinksnap.model.dto.req.LoginReq;
import com.wz.xlinksnap.model.dto.req.RegisterReq;
import com.wz.xlinksnap.model.dto.resp.LoginResp;
import com.wz.xlinksnap.model.entity.User;
import com.wz.xlinksnap.mapper.UserMapper;
import com.wz.xlinksnap.service.MessageService;
import com.wz.xlinksnap.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-27
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private MessageService messageService;

    /**
     * 注册
     */
    @Override
    public void register(RegisterReq registerReq) {
        String phone = registerReq.getPhone();
        String email = registerReq.getEmail();
        String code = registerReq.getCode();
        //1.根据email或者phone查询是否已经注册过
        if (StringUtils.isEmpty(phone) && getByPhone(phone) != null) {
            throw new ConditionException("邮箱已经被注册过了！");
        }
        if (StringUtils.isEmpty(email) && getByEmail(email) != null) {
            throw new ConditionException("手机号已经被注册过了！");
        }
        //2.验证码验证
        if (!messageService.verifyCode(email == null ? phone : email, code)) {
            throw new ConditionException("验证码错误！");
        }
        //3.密码加密
        String md5Password = MD5Util.MD5(registerReq.getPassword());
        //4.创建User对象
        User user = new User()
                .setPhone(StringUtils.isEmpty(phone) ? "" : phone)
                .setEmail(StringUtils.isEmpty(email) ? "" : email)
                .setUsername(registerReq.getUsername())
                .setPassword(md5Password);
        //5.插入数据库
        baseMapper.insert(user);
    }

    /**
     * 登录
     */
    @Override
    public LoginResp login(LoginReq loginReq) {
        String username = loginReq.getUsername();
        String password = loginReq.getPassword();
        //1.验证是否已注册
        User dbUser = getByUsername(username);
        if(dbUser == null) {
            throw new ConditionException("用户还未注册！");
        }
        //2.验证密码是否正确
        if(!MD5Util.valid(password,dbUser.getPassword())) {
            throw new ConditionException("密码不正确！");
        }
        //3.登录
        StpUtil.login(dbUser.getId());
        //4.返回token
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return LoginResp.builder()
                .tokenName(tokenInfo.getTokenName())
                .tokenValue(tokenInfo.getTokenValue())
                .build()
    }

    /**
     * 根据用户名获取用户（假设用户名是唯一的）
     */
    @Override
    public User getByUsername(String username) {
        return baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    /**
     * 根据phone查询User
     */
    @Override
    public User getByPhone(String phone) {
        return baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
    }

    /**
     * 根据email查询User
     */
    @Override
    public User getByEmail(String email) {
        return baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
    }

    /**
     * 发送邮箱验证码
     */
    @Override
    public void sendCodeByEmail(String email) {
        messageService.sendCodeByEmail(email);
    }

    /**
     * 发送手机验证码
     */
    @Override
    public void sendCodeByPhone(String phone) {
        messageService.sendCodeByPhone(phone);
    }


}
