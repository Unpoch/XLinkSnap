package com.wz.xlinksnap.service.impl;

import com.wz.xlinksnap.common.constant.RedisConstant;
import com.wz.xlinksnap.common.exception.ConditionException;
import com.wz.xlinksnap.common.util.Base62Converter;
import com.wz.xlinksnap.common.util.TimeUtil;
import com.wz.xlinksnap.common.util.UrlUtil;
import com.wz.xlinksnap.model.dto.req.CreateShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.CreateShortUrlReq;
import com.wz.xlinksnap.model.entity.ShortUrl;
import com.wz.xlinksnap.mapper.ShortUrlMapper;
import com.wz.xlinksnap.service.BloomFilterService;
import com.wz.xlinksnap.service.IdGenerationService;
import com.wz.xlinksnap.service.ShortUrlService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 长短链接映射表 服务实现类
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-27
 */
@Service
public class ShortUrlServiceImpl extends ServiceImpl<ShortUrlMapper, ShortUrl> implements ShortUrlService {

    @Autowired
    private IdGenerationService idGenerationService;

    @Autowired
    private BloomFilterService bloomFilterService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 创建短链
     * 二义性检查
     */
    //TODO: 分布式锁注解实现
    @Override
    public CreateShortUrlResp createShortUrl(CreateShortUrlReq createShortUrlReq) {
        String lurl = createShortUrlReq.getLurl();
        String domain = createShortUrlReq.getDomain();
        LocalDateTime validTime = createShortUrlReq.getValidTime();
        //分布式锁
        RLock lock = redissonClient.getLock(RedisConstant.CREATE_SURL_LOCK + lurl);
        lock.lock();
        try {
            //0.二义性检查，是否已经为该长链生成短链
            //布隆过滤器判断，如果已经存在，则直接查询缓存（数据库）中的返回
            if (bloomFilterService.containLUrl(lurl)) {
                //长短链接的一一映射关系
                String surl = redisTemplate.opsForValue().get(RedisConstant.LONG_URL_KEY + lurl);
                if (surl != null) //真的会出现这种情况？布隆过滤器和缓存是同步写入的
                    return CreateShortUrlResp.builder().surl(surl).lurl(lurl).build();
                //TODO：进一步查询数据库确认长链接是否存在
            }
            //1.生成全局唯一id
            long surlId = idGenerationService.generateId();
            //2.唯一id转为62进制 字符串
            String suffix = Base62Converter.encode(surlId);
            //3.域名 + 唯一id => 短链
            String surl = UrlUtil.buildShortUrl(domain, suffix);
            //4.构建ShortUrl对象插入数据库（可以异步处理）
            ShortUrl shortUrl = new ShortUrl()
                    .setSurlId(surlId)
                    .setLurl(lurl)
                    .setSurl(surl)
                    .setValidTime(validTime);
            baseMapper.insert(shortUrl);
            //5.添加到布隆过滤器 和 缓存中 TODO：异步执行
            bloomFilterService.addLUrl(lurl);
            //计算当前时间和有效期的差值（精确到分钟）
            long minutes = TimeUtil.calculateDifference(LocalDateTime.now(), validTime, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(RedisConstant.LONG_URL_KEY + lurl, surl, minutes, TimeUnit.MINUTES);
            //6.生成CreateShortUrlResp返回
            return CreateShortUrlResp
                    .builder()
                    .surl(surl)
                    .lurl(lurl)
                    .build();
        } catch (Exception e) {
            throw new ConditionException("生成短链失败！");
        } finally {
            lock.unlock();
        }
    }
}
