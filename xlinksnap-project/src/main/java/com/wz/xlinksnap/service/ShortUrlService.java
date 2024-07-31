package com.wz.xlinksnap.service;

import com.wz.xlinksnap.model.dto.req.BatchCreateShortUrlReq;
import com.wz.xlinksnap.model.dto.req.PageShortUrlReq;
import com.wz.xlinksnap.model.dto.req.QueryGroupShortUrlCountReq;
import com.wz.xlinksnap.model.dto.resp.BatchCreateShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.CreateShortUrlResp;
import com.wz.xlinksnap.model.dto.req.CreateShortUrlReq;
import com.wz.xlinksnap.model.dto.resp.PageShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.QueryGroupShortUrlCountResp;
import com.wz.xlinksnap.model.entity.ShortUrl;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.List;

/**
 * <p>
 * 长短链接映射表 服务类
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-27
 */
public interface ShortUrlService extends IService<ShortUrl> {

    CreateShortUrlResp createShortUrl(CreateShortUrlReq createShortUrlReq);

    void redirect(String surl, ServletRequest request, ServletResponse response);

    BatchCreateShortUrlResp batchCreateShortUrl(BatchCreateShortUrlReq batchCreateShortUrlReq);

    ShortUrl getShortUrlBySurlId(Long surlId);

    void batchInsertShortUrl(List<ShortUrl> shortUrlList);

    PageShortUrlResp<ShortUrl> pageShortUrl(PageShortUrlReq pageShortUrlReq);

    List<QueryGroupShortUrlCountResp> queryGroupShortUrlCount(QueryGroupShortUrlCountReq queryGroupShortUrlCountReq);

    List<ShortUrl> getShortUrlListByGroupIds(Set<Long> groupIds);
}
