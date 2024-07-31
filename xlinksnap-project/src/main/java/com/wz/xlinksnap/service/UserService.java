package com.wz.xlinksnap.service;

import com.wz.xlinksnap.model.dto.req.LoginReq;
import com.wz.xlinksnap.model.dto.req.RegisterReq;
import com.wz.xlinksnap.model.dto.resp.LoginResp;
import com.wz.xlinksnap.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-27
 */
public interface UserService extends IService<User> {

    void register(RegisterReq registerReq);

    User getByPhone(String phone);

    User getByEmail(String email);

    void sendCodeByEmail(String email);

    void sendCodeByPhone(String phone);

    LoginResp login(LoginReq loginReq);

    User getByUsername(String username);
}
