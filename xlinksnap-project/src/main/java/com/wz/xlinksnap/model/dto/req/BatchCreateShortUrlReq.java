package com.wz.xlinksnap.model.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量创建短链请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchCreateShortUrlReq {

    /**
     * 长链接集合
     */
    private List<String> lurlList;

    /**
     * 域名（目前只支持同一批域名下的短链接的创建）
     */
    private String domain;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 有效期
     */
    private LocalDateTime validTime;
}