package com.wz.xlinksnap.service.impl;

import com.wz.xlinksnap.model.entity.User;
import com.wz.xlinksnap.mapper.UserMapper;
import com.wz.xlinksnap.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
