package com.wz.xlinksnap.model.dto.req;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginReq {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空号")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
