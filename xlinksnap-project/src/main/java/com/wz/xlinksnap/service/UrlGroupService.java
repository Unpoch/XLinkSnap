package com.wz.xlinksnap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wz.xlinksnap.model.dto.req.AddUrlGroupReq;
import com.wz.xlinksnap.model.dto.req.QueryGroupShortUrlCountReq;
import com.wz.xlinksnap.model.dto.req.UpdateUrlGroupReq;
import com.wz.xlinksnap.model.dto.resp.AddUrlGroupResp;
import com.wz.xlinksnap.model.dto.resp.QueryGroupShortUrlCountResp;
import com.wz.xlinksnap.model.entity.UrlGroup;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 短链分组表 服务类
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-30
 */
public interface UrlGroupService extends IService<UrlGroup> {

    AddUrlGroupResp addUrlGroup(AddUrlGroupReq addUrlGroupReq);

    List<UrlGroup> getUrlGroupListByUserId(Long userId);

    List<UrlGroup> getUrlGroupListByGroupIdsAndUserId(Long userId, Set<Long> groupIds);

    void updateUrlGroup(UpdateUrlGroupReq updateUrlGroupReq);
}
