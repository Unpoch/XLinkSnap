package com.wz.xlinksnap.model.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新短链分组请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUrlGroupReq {

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 分组名称
     */
    private String name;


}
