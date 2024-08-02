package com.wz.xlinksnap.model.dto.req;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链续期请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenewalShortUrlReq {

    /**
     * 完整短链集合
     */
    private List<String> surlList;

    /**
     * 下一个有效期
     */
    private LocalDateTime nextValidTime;
}
