package com.wz.xlinksnap.model.dto.req;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询短链分组所有短链数量请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryGroupShortUrlCountReq {

    /**
     * groupId集合
     */
    private List<Long> groupIdList;
}
