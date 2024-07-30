package com.wz.xlinksnap.model.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询短链请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageShortUrlReq {

    /**
     * 当前页
     */
    private Integer pageNo;

    /**
     * 每页记录数
     */
    private Integer pageSize;

    /**
     * 分组id
     */
    private Long groupId;
}
