package com.wz.xlinksnap.model.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量创建短链响应对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchCreateShortUrlResp {

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 短链-长链映射集合
     */
    private List<BatchCreateShortUrlMappingResp> mappingUrlList;
}