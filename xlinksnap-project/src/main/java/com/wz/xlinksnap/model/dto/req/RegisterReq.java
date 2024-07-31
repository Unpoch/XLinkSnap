package com.wz.xlinksnap.model.dto.req;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterReq {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空号")
    private String username;

    /**
     * 手机号
     */
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
}
