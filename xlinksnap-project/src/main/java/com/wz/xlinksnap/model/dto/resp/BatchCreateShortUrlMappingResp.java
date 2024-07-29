package com.wz.xlinksnap.model.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量创建短链映射 响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchCreateShortUrlMappingResp {

    /**
     * 短链
     */
    private String surl;

    /**
     * 长链
     */
    private String lurl;
}