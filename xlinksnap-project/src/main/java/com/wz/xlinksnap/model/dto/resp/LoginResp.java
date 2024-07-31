package com.wz.xlinksnap.model.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResp {

    /**
     * token名称
     */
    private String tokenName;

    /**
     * token值
     */
    private String tokenValue;
}
