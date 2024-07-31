package com.wz.xlinksnap.model.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询分组短链数量响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryGroupShortUrlCountResp {

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 短链数量
     */
    private Integer shortUrlCount;
}
