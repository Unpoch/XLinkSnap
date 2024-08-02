package com.wz.xlinksnap.model.dto.resp;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链续期响应对象（不太需要这个对象），返回success就可以了
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenewalShortUrlResp {

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 下一个有效期
     */
    private LocalDateTime nextValidTime;
}
