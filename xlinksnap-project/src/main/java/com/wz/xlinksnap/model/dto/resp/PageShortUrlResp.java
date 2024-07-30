package com.wz.xlinksnap.model.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页短链响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageShortUrlResp<T> {
    /**
     * 记录数
     */
    private long total;

    /**
     * 分页记录
     */
    private List<T> records;
}
