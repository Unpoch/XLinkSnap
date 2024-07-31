package com.wz.xlinksnap.controller;


import com.baomidou.mybatisplus.extension.api.R;
import com.wz.xlinksnap.common.result.Result;
import com.wz.xlinksnap.model.dto.req.RegisterReq;
import com.wz.xlinksnap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-27
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * TODO:登录
     */

    /**
     * TODO:注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterReq registerReq) {
        userService.register(registerReq);
        return Result.success();
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendCodeByEmail")
    public Result<String> sendCodeByEmail(@RequestParam String email) {
        userService.sendCodeByEmail(email);
        return Result.success();
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sendCodeByPhone")
    public Result<String> sendCodeByPhone(@RequestParam String phone) {
        userService.sendCodeByPhone(phone);
        return Result.success();
    }




}

