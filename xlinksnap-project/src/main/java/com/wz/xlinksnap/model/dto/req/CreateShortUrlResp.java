package com.wz.xlinksnap.model.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建短链响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShortUrlResp {

    /**
     * 短链
     */
    private String surl;

    /**
     * 长链
     */
    private String lurl;
}
