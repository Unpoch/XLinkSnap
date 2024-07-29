package com.wz.xlinksnap.model.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建短链请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShortUrlReq {

    /**
     * 域名
     */
    private String domain;

    /**
     * 长链
     */
    private String lurl;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validTime;
}
