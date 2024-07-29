package com.wz.xlinksnap.mapper;

import com.wz.xlinksnap.model.entity.ShortUrl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 长短链接映射表 Mapper 接口
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-27
 */
public interface ShortUrlMapper extends BaseMapper<ShortUrl> {

    void batchInsertShortUrl(List<ShortUrl> shortUrlList);
}
