package com.wz.xlinksnap.model.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建短链分组响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddUrlGroupResp {

    /**
     * 分组id
     */
    private Long groupId;
}
