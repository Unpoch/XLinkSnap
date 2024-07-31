package com.wz.xlinksnap.model.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建短链分组请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddUrlGroupReq {

    /**
     * 分组名称
     */
    private String name;

    /**
     * 用户id
     */
    private Long userId;

}
