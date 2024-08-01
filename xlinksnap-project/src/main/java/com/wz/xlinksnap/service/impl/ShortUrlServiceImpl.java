package com.wz.xlinksnap.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wz.xlinksnap.common.annotation.DistributedRLock;
import com.wz.xlinksnap.common.constant.RedisConstant;
import com.wz.xlinksnap.common.exception.ConditionException;
import com.wz.xlinksnap.common.util.Base62Converter;
import com.wz.xlinksnap.common.util.Base64Converter;
import com.wz.xlinksnap.common.util.TimeUtil;
import com.wz.xlinksnap.common.util.UrlUtil;
import com.wz.xlinksnap.model.dto.req.BatchCreateShortUrlReq;
import com.wz.xlinksnap.model.dto.req.PageShortUrlReq;
import com.wz.xlinksnap.model.dto.req.QueryGroupShortUrlCountReq;
import com.wz.xlinksnap.model.dto.resp.BatchCreateShortUrlMappingResp;
import com.wz.xlinksnap.model.dto.resp.BatchCreateShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.CreateShortUrlResp;
import com.wz.xlinksnap.model.dto.req.CreateShortUrlReq;
import com.wz.xlinksnap.model.dto.resp.PageShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.QueryGroupShortUrlCountResp;
import com.wz.xlinksnap.model.entity.ShortUrl;
import com.wz.xlinksnap.mapper.ShortUrlMapper;
import com.wz.xlinksnap.model.entity.UrlGroup;
import com.wz.xlinksnap.model.excel.ShortUrlExport;
import com.wz.xlinksnap.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@Slf4j
public class ShortUrlServiceImpl extends ServiceImpl<ShortUrlMapper, ShortUrl> implements ShortUrlService {

    @Autowired
    private IdGenerationService idGenerationService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private UrlGroupService urlGroupService;

    @Autowired
    private ShortUrlMapper shortUrlMapper;

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
    @DistributedRLock(prefix = RedisConstant.CREATE_SURL_LOCK)
    public CreateShortUrlResp createShortUrl(CreateShortUrlReq createShortUrlReq) {
        String lurl = createShortUrlReq.getLurl();
        String domain = createShortUrlReq.getDomain();
        Long groupId = createShortUrlReq.getGroupId();
        LocalDateTime validTime = createShortUrlReq.getValidTime();
        String lurlKey = Base64Converter.encodeToBase64(lurl);//作为布隆过滤器和redis的key
        //分布式锁
        // RLock lock = redissonClient.getLock(RedisConstant.CREATE_SURL_LOCK + lurl);
        // lock.lock();
        try {
            //0.二义性检查，是否已经为该长链生成短链
            //布隆过滤器判断，如果已经存在，则直接查询缓存（数据库）中的返回
            if (bloomFilterService.containLUrl(lurlKey)) {
                //长短链接的一一映射关系
                String surl = redisTemplate.opsForValue().get(RedisConstant.LONG_URL_KEY + lurlKey);
                if (surl != null) {//真的会出现这种情况？布隆过滤器和缓存是同步写入的
                    return CreateShortUrlResp.builder().surl(surl).lurl(lurl).build();
                }
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
                    .setSuffix(suffix)
                    .setLurl(lurl)
                    .setSurl(surl)
                    .setGroupId(groupId)
                    .setValidTime(validTime)
                    .setPV(0)
                    .setUV(0)
                    .setVV(0)
                    .setIP(0);
            baseMapper.insert(shortUrl);
            //5.添加到布隆过滤器 和 缓存中 TODO：异步执行
            bloomFilterService.addLUrl(lurlKey);
            //计算当前时间和有效期的差值（精确到分钟）
            long minutes = TimeUtil.calculateDifference(LocalDateTime.now(), validTime, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(RedisConstant.LONG_URL_KEY + lurlKey, surl, minutes, TimeUnit.MINUTES);
            //6.生成CreateShortUrlResp返回
            return CreateShortUrlResp
                    .builder()
                    .surl(surl)
                    .lurl(lurl)
                    .groupId(groupId)
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ConditionException("生成短链失败！");
        } finally {
            // lock.unlock();
        }
    }

    /**
     * 重定向短链接到长链接
     * 统计指标
     */
    @Override
    public void redirect(String surl, ServletRequest request, ServletResponse response) {
        try {
            //获取短链的suffix -> base62 变成唯一id -> 根据唯一id查询长链（缓存？MySQL）
            String suffix = UrlUtil.getShortUrlSuffix(surl);
            //1.统计指标 PV,UV,VV,IP
            metricsService.setDailyMetrics(suffix, (HttpServletRequest) request, (HttpServletResponse) response);
            //2.查询短链对应的长链
            long surlId = Base62Converter.decode(suffix);//Base62解码获取唯一id
            ShortUrl shortUrl = getShortUrlBySurlId(surlId);
            String lurl = shortUrl.getLurl();
            //3.重定向
            ((HttpServletResponse) response).sendRedirect(lurl);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ConditionException("403", "页面找不到！！");
        }
    }

    /**
     * 批量创建短链
     * 目前仅支持同一域名下的短链创建
     * TODO:是否需要分布式锁？
     */
    @Override
    public BatchCreateShortUrlResp batchCreateShortUrl(BatchCreateShortUrlReq batchCreateShortUrlReq) {
        List<String> lurlList = batchCreateShortUrlReq.getLurlList();
        String domain = batchCreateShortUrlReq.getDomain();
        Long groupId = batchCreateShortUrlReq.getGroupId();
        LocalDateTime validTime = batchCreateShortUrlReq.getValidTime();
        List<ShortUrl> shortUrlList = new ArrayList<>();
        List<BatchCreateShortUrlMappingResp> result = new ArrayList<>();
        //1.遍历集合，二义性检查
        //2.每个元素，生成唯一id，转换62进制，并生成短链
        lurlList.forEach(lurl -> {
            String lurlKey = Base64Converter.encodeToBase64(lurl);
            String surl = "";
            //如果lurl已经转换过
            if (bloomFilterService.containLUrl(lurlKey)) {
                //TODO:是否有必要如此做，误判？抛异常？
                surl = redisTemplate.opsForValue().get(RedisConstant.LONG_URL_KEY + lurlKey);
                if (surl == null) { //缓存中也没有
                    return;//相当于continue
                }
            }
            long surlId = idGenerationService.generateId();
            String suffix = Base62Converter.encode(surlId);
            //不为空说明从缓存中拿到了，说明布隆过滤器误判？
            if (StringUtils.isEmpty(surl)) {
                surl = UrlUtil.buildShortUrl(domain, suffix);
            }
            //创建短链 - 长链映射对象
            BatchCreateShortUrlMappingResp urlMappingResp = BatchCreateShortUrlMappingResp.builder()
                    .lurl(lurl)
                    .surl(surl)
                    .build();
            result.add(urlMappingResp);//添加到结果中
            //创建ShortUrl对象
            ShortUrl shortUrl = new ShortUrl()
                    .setLurl(lurl)
                    .setSurl(surl)
                    .setSurlId(surlId)
                    .setSuffix(suffix)
                    .setGroupId(groupId)
                    .setValidTime(validTime)
                    .setPV(0)
                    .setUV(0)
                    .setVV(0)
                    .setIP(0);
            shortUrlList.add(shortUrl);
        });
        //暂时用Executors创建线程池（不推荐），后续配置线程池类
        ExecutorService executor = Executors.newFixedThreadPool(5);
        //3.批量插入数据库（异步执行）
        CompletableFuture.runAsync(() -> {
            batchInsertShortUrl(shortUrlList);
        }, executor);
        //4.批量添加到缓存和布隆过滤器中（异步执行）
        CompletableFuture.runAsync(() -> {
            batchAddLUrlTOCacheAndBloom(shortUrlList);
        }, executor);
        //5.构建响应对象返回
        return BatchCreateShortUrlResp.builder().mappingUrlList(result).groupId(groupId).build();
    }

    /**
     * 批量添加lurlKey到布隆过滤器和缓存中
     */
    private void batchAddLUrlTOCacheAndBloom(List<ShortUrl> ShortUrl) {
        ShortUrl.forEach(shortUrl -> {
            String lurlKey = Base64Converter.encodeToBase64(shortUrl.getLurl());
            String surl = shortUrl.getSurl();
            LocalDateTime validTime = shortUrl.getValidTime();
            long minutes = TimeUtil.calculateDifference(LocalDateTime.now(), validTime, TimeUnit.MINUTES);
            // 添加到布隆过滤器
            bloomFilterService.addLUrl(lurlKey);
            // 添加到缓存
            redisTemplate.opsForValue().set(RedisConstant.LONG_URL_KEY + lurlKey, surl, minutes, TimeUnit.MINUTES);
        });
    }

    /**
     * 批量插入数据库
     */
    @Override
    public void batchInsertShortUrl(List<ShortUrl> shortUrlList) {
        shortUrlMapper.batchInsertShortUrl(shortUrlList);
    }

    /**
     * 分页查询短链
     */
    @Override
    public PageShortUrlResp<ShortUrl> pageShortUrl(PageShortUrlReq pageShortUrlReq) {
        //1.构建分页参数
        Integer pageNo = pageShortUrlReq.getPageNo();
        Integer pageSize = pageShortUrlReq.getPageSize();
        int start = (pageNo - 1) * pageSize;
        Long groupId = pageShortUrlReq.getGroupId();
        Page<ShortUrl> pageParams = new Page<>(start, pageSize);
        //2.分页查询
        IPage<ShortUrl> page = baseMapper.selectPage(pageParams, new LambdaQueryWrapper<ShortUrl>()
                .eq(groupId != null, ShortUrl::getGroupId, groupId)
                .ge(ShortUrl::getValidTime, LocalDateTime.now()) //未过期的
                .eq(ShortUrl::getIsDeleted, 0)  //未被删除的
                .orderByDesc(ShortUrl::getSurlId));
        return PageShortUrlResp
                .<ShortUrl>builder()
                .total(page.getTotal())
                .records(page.getRecords())
                .build();
    }

    /**
     * 查询分组下短链数量
     */
    @Override
    public List<QueryGroupShortUrlCountResp> queryGroupShortUrlCount(
            QueryGroupShortUrlCountReq queryGroupShortUrlCountReq) {
        //1.获取参数
        List<Long> groupIdList = queryGroupShortUrlCountReq.getGroupIdList();
        Set<Long> groupIdSet = new HashSet<>(groupIdList);
        //2.根据groupId集合查询所有短链
        List<ShortUrl> shortUrlList = getShortUrlListByGroupIds(groupIdSet);
        //3.映射groupId -> ShortUrl的个数
        Map<Long, Long> countMap = shortUrlList.stream().collect(Collectors.groupingBy(ShortUrl::getGroupId,
                Collectors.counting()));//按照groupId分组后统计个数
        //4.构建响应对象
        List<QueryGroupShortUrlCountResp> list = countMap.entrySet().stream()
                .map(entry -> QueryGroupShortUrlCountResp
                        .builder()
                        .groupId(entry.getKey())
                        .shortUrlCount(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());
        return list;
    }

    /**
     * 根据短链分组id集合获取所有短链
     */
    @Override
    public List<ShortUrl> getShortUrlListByGroupIds(Set<Long> groupIds) {
        return baseMapper.selectList(new LambdaQueryWrapper<ShortUrl>()
                .in(ShortUrl::getGroupId, groupIds)
                .eq(ShortUrl::getIsDeleted, 0));
    }

    /**
     * 查询所有未过期的且未被删除的的短链
     */
    @Override
    public List<ShortUrl> getAllUnexpiredShortUrl(LocalDateTime now) {
        return baseMapper.selectList(new LambdaQueryWrapper<ShortUrl>()
                .ge(ShortUrl::getValidTime, now)
                .eq(ShortUrl::getIsDeleted, 0));
    }

    /**
     * 批量更新ShortUrl
     */
    @Override
    public void batchUpdateShortUrl(List<ShortUrl> shortUrlList) {
        shortUrlMapper.batchUpdateShortUrl(shortUrlList);
    }

    /**
     * 导出短链接excel数据表
     */
    @Override
    public void exportExcel(Long userId, HttpServletResponse response) {
        //1.根据userId查询分组集合，筛选分组id
        List<UrlGroup> urlGroupList = urlGroupService.getGroupIdsByUserId(userId);
        Set<Long> groupIdSet = urlGroupList.stream().map(UrlGroup::getId).collect(Collectors.toSet());
        //2.根据分组id集合查询所有短链接
        List<ShortUrl> shortUrlList = getShortUrlListByGroupIds(groupIdSet);
        //3.将短链接对象转换为ShortUrlExport
        List<ShortUrlExport> shortUrlExportList = shortUrlList.stream().map(shortUrl -> new ShortUrlExport()
                .setSurlId(shortUrl.getSurlId())
                .setSurl(shortUrl.getSurl())
                .setLurl(shortUrl.getLurl())
                .setPV(shortUrl.getPV())
                .setUV(shortUrl.getUV())
                .setVV(shortUrl.getVV())
                .setIP(shortUrl.getIP())
                .setValidTime(shortUrl.getValidTime())).collect(Collectors.toList());
        //4.设置响应头
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("短链数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            //5.导出数据
            EasyExcel.write(response.getOutputStream(), ShortUrlExport.class).sheet("短链数据").doWrite(shortUrlExportList);
        } catch (Exception e) {
            log.error("导出数据失败" + "用户：" + userId);
            throw new ConditionException("导出数据失败！");
        }

    }

    /**
     * 查询所有未被删除，且已过期的短链
     */
    @Override
    public List<ShortUrl> getAllExpiredShortUrl(LocalDateTime now) {
        return baseMapper.selectList(new LambdaQueryWrapper<ShortUrl>()
                .ge(ShortUrl::getValidTime, now)
                .eq(ShortUrl::getIsDeleted, 0));
    }

    /**
     * 分页查询所有已过期短链 和 已删除短链
     */
    @Override
    public PageShortUrlResp<ShortUrl> pageExpiredDeletedSurl(PageShortUrlReq pageShortUrlReq) {
        Integer pageNo = pageShortUrlReq.getPageNo();
        Integer pageSize = pageShortUrlReq.getPageSize();
        Long groupId = pageShortUrlReq.getGroupId();
        int start = (pageNo - 1) * pageSize;//从第几条开始查
        //1.构建分页参数
        Page<ShortUrl> pageParams = new Page<>(start, pageSize);
        //2.分页查询
        IPage<ShortUrl> page = baseMapper.selectPage(pageParams, new LambdaQueryWrapper<ShortUrl>()
                .eq(groupId != null, ShortUrl::getGroupId, groupId)
                .le(ShortUrl::getValidTime, LocalDateTime.now()) //过期的
                .eq(ShortUrl::getIsDeleted, 1) //已删除的
                .orderByDesc(ShortUrl::getSurlId));
        //3.构建响应对象返回
        return PageShortUrlResp
                .<ShortUrl>builder()
                .total(page.getTotal())
                .records(page.getRecords())
                .build();
    }


    /**
     * 根据短链id获取短链对象
     */
    @Override
    public ShortUrl getShortUrlBySurlId(Long surlId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<ShortUrl>()
                .eq(ShortUrl::getSurlId, surlId)
                .eq(ShortUrl::getIsDeleted, 0));
    }
}
