package com.wz.xlinksnap.service;

import com.wz.xlinksnap.model.dto.req.CreateShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.CreateShortUrlReq;
import com.wz.xlinksnap.model.entity.ShortUrl;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
