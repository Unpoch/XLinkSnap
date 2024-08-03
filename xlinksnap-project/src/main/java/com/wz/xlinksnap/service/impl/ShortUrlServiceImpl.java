package com.wz.xlinksnap.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wz.xlinksnap.common.annotation.DistributedRLock;
import com.wz.xlinksnap.common.concurrent.ConcurrentJobExecutor;
import com.wz.xlinksnap.common.constant.MessageConstant;
import com.wz.xlinksnap.common.constant.RedisConstant;
import com.wz.xlinksnap.common.exception.ConditionException;
import com.wz.xlinksnap.common.util.Base62Converter;
import com.wz.xlinksnap.common.util.Base64Converter;
import com.wz.xlinksnap.common.util.TimeUtil;
import com.wz.xlinksnap.common.util.UrlUtil;
import com.wz.xlinksnap.model.dto.req.BatchCreateShortUrlReq;
import com.wz.xlinksnap.model.dto.req.BatchSendMessageReq;
import com.wz.xlinksnap.model.dto.req.PageShortUrlReq;
import com.wz.xlinksnap.model.dto.req.QueryGroupShortUrlCountReq;
import com.wz.xlinksnap.model.dto.req.RenewalShortUrlReq;
import com.wz.xlinksnap.model.dto.req.SendMessageReq;
import com.wz.xlinksnap.model.dto.resp.BatchCreateShortUrlMappingResp;
import com.wz.xlinksnap.model.dto.resp.BatchCreateShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.CreateShortUrlResp;
import com.wz.xlinksnap.model.dto.req.CreateShortUrlReq;
import com.wz.xlinksnap.model.dto.resp.PageShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.QueryGroupShortUrlCountResp;
import com.wz.xlinksnap.model.dto.resp.RenewalShortUrlResp;
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
import java.util.concurrent.ScheduledFuture;
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

import org.springframework.util.CollectionUtils;

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
    private MessageService messageService;

    @Autowired
    private ShortUrlMapper shortUrlMapper;

    @Autowired
    private BloomFilterService bloomFilterService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DynamicTaskService dynamicTaskService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ConcurrentJobExecutor concurrentJobExecutor;

    /**
     * 创建短链
     * 二义性检查
     */
    @Override
    @DistributedRLock(prefix = RedisConstant.CREATE_SURL_LOCK)
    public CreateShortUrlResp createShortUrl(CreateShortUrlReq createShortUrlReq) {
        String lurl = createShortUrlReq.getLurl();
        String domain = createShortUrlReq.getDomain();
        Long groupId = createShortUrlReq.getGroupId();
        LocalDateTime validTime = createShortUrlReq.getValidTime();
        String lurlKey = Base64Converter.encodeToBase64(lurl);//作为布隆过滤器和redis的key
        try {
            //0.二义性检查，是否已经为该长链生成短链
            //布隆过滤器判断，如果已经存在，则直接查询缓存（数据库）中的返回
            if (bloomFilterService.containLUrl(lurlKey)) {
                //误判：认为你在你却不在；如何判断：布隆过滤器中判断存在，数据库和缓存判断不存在
                //长短链接的一一映射关系
                String surl = redisTemplate.opsForValue().get(RedisConstant.LONG_URL_KEY + lurlKey);
                if (surl != null) {
                    return CreateShortUrlResp.builder().surl(surl).lurl(lurl).groupId(groupId).build();
                }

                //TODO:假设长链已存在，10w请求为同一长链创建短链，缓存未命中的情况下，大量请求打到数据库
                //      可以考虑多级缓存！
                ShortUrl shortUrl = getShortUrlByLurl(lurl);
                if (shortUrl != null) {
                    long minutes = TimeUtil.calculateDifference(LocalDateTime.now(), shortUrl.getValidTime(), TimeUnit.MINUTES);
                    redisTemplate.opsForValue().set(RedisConstant.LONG_URL_KEY + lurlKey, shortUrl.getSurl(), minutes, TimeUnit.MINUTES);
                    return CreateShortUrlResp.builder().surl(shortUrl.getSurl()).lurl(lurl).groupId(shortUrl.getGroupId()).build();
                }
                //走到这里才是误判
                log.warn("布隆过滤器误判！lurlKey:{}", lurlKey);
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
            //5.添加到布隆过滤器 和 缓存中
            concurrentJobExecutor.runAsync(() -> {
                bloomFilterService.addLUrl(lurlKey);
            });
            //计算当前时间和有效期的差值（精确到分钟）
            long minutes = TimeUtil.calculateDifference(LocalDateTime.now(), validTime, TimeUnit.MINUTES);
            concurrentJobExecutor.runAsync(() -> {
                redisTemplate.opsForValue().set(RedisConstant.LONG_URL_KEY + lurlKey, surl, minutes, TimeUnit.MINUTES);
            });
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
        }
    }

    /**
     * 重定向短链接到长链接
     * 统计指标
     */
    @Override
    public void redirect(String surl, ServletRequest request, ServletResponse response) {
        try {
            //1.获取短链的suffix -> base62 变成唯一id
            String suffix = UrlUtil.getShortUrlSuffix(surl);
            long surlId = Base62Converter.decode(suffix);//Base62解码获取唯一id
            //2.根据唯一id查询ShortUrl
            ShortUrl shortUrl = getShortUrlBySurlId(surlId);
            //检查短链是否过期，过期抛异常，访问的链接不存在
            //空说明被删除了
            if (shortUrl == null || shortUrl.getValidTime().isBefore(LocalDateTime.now())) {
                log.info("短链接：{} 过期或已被删除", surl);
                throw new ConditionException("404", "您访问的链接已失效！");
            }
            //2.统计指标 PV,UV,VV,IP
            metricsService.setDailyMetrics(suffix, (HttpServletRequest) request, (HttpServletResponse) response);
            //3.获取长链
            String lurl = shortUrl.getLurl();
            //4.重定向
            ((HttpServletResponse) response).sendRedirect(lurl);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ConditionException("404", "页面找不到！！");
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
                surl = redisTemplate.opsForValue().get(RedisConstant.LONG_URL_KEY + lurlKey);
                //缓存中存在，说明已经创建了
                if (surl != null) {
                    return;
                }
                //缓存中不存在，查询数据库
                ShortUrl shortUrl = getShortUrlByLurl(lurl);
                //数据库存在
                if (shortUrl != null) {
                    long minutes = TimeUtil.calculateDifference(LocalDateTime.now(), shortUrl.getValidTime(), TimeUnit.MINUTES);
                    redisTemplate.opsForValue().set(RedisConstant.LONG_URL_KEY + lurlKey, shortUrl.getSurl(), minutes, TimeUnit.MINUTES);
                    return;
                }
                //走到这，说明缓存和数据库都不存在，说明布隆过滤器误判了
                log.warn("布隆过滤器误判！lurlKey:{}", lurlKey);
                //那么会走下面创建的逻辑
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
        //3.批量插入数据库（异步执行）
        concurrentJobExecutor.runAsync(() -> {
            batchInsertShortUrl(shortUrlList);
        });
        //4.批量添加到缓存和布隆过滤器中（异步执行）
        concurrentJobExecutor.runAsync(() -> {
            batchAddLUrlTOCacheAndBloom(shortUrlList);
        });
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
            EasyExcel.write(response.getOutputStream(), ShortUrlExport.class).sheet("短链数据")
                    .doWrite(shortUrlExportList);
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
     * 短链续期
     * 未过期延长有效期
     * 过期，过期才会被删除
     * 被删除的续期，直接恢复未删除，且延长有效期
     */
    @Override
    public RenewalShortUrlResp renewalShortUrl(RenewalShortUrlReq renewalShortUrlReq) {
        //1.获取参数
        List<String> surlList = renewalShortUrlReq.getSurlList();
        LocalDateTime nextValidTime = renewalShortUrlReq.getNextValidTime();
        int successCount = 0;
        //2.surlList -> 后缀suffix -> 短链唯一id集合
        Set<Long> surlIdSet = surlList.stream().map(surl -> Base62Converter.decode(UrlUtil.getShortUrlSuffix(surl)))
                .collect(Collectors.toSet());
        //3.根据短链唯一id查询所有短链对象（不管是否过期，是否删除）
        List<ShortUrl> shortUrlList = getShortUrlListBySurlIds(surlIdSet);
        //4.为这些短链续期，如果删除的要恢复，其实全部设置为0，就没有那么多判断了
        //shortUrlList.forEach(shortUrl -> {}); lambda表达式中操作变量必须为final或者原子类型
        for (ShortUrl shortUrl : shortUrlList) {
            //有效期必须在设置的有效期之前
            if (shortUrl.getValidTime().isBefore(nextValidTime)) {
                shortUrl.setIsDeleted(0);
                shortUrl.setValidTime(nextValidTime);
                successCount++;
            }
        }
        //5.批量更新数据库
        batchUpdateShortUrl(shortUrlList);
        //6.构建响应对象
        return RenewalShortUrlResp
                .builder()
                .successCount(successCount)
                .nextValidTime(nextValidTime)
                .build();
    }

    /**
     * 根据短链id集合查询所有短链
     */
    @Override
    public List<ShortUrl> getShortUrlListBySurlIds(Set<Long> surlIds) {
        return baseMapper.selectList(new LambdaQueryWrapper<ShortUrl>()
                .in(ShortUrl::getSurlId, surlIds));
    }

    /**
     * 发送信息
     */
    @Override
    public void sendMessage(SendMessageReq sendMessageReq) {
        //1.获取参数
        String email = sendMessageReq.getEmail();
        String phone = sendMessageReq.getPhone();
        String subject = sendMessageReq.getSubject();
        String msgBody = sendMessageReq.getMsgBody();
        String sendType = sendMessageReq.getSendType();
        LocalDateTime sendTime = sendMessageReq.getSendTime();
        //2.为长链创建短链，需要创建短链请求对象
        CreateShortUrlReq createShortUrlReq = CreateShortUrlReq.builder()
                .lurl(sendMessageReq.getLurl())
                .domain(sendMessageReq.getDomain())
                .groupId(sendMessageReq.getGroupId())
                .validTime(sendMessageReq.getValidTime()).build();
        CreateShortUrlResp createShortUrlResp = createShortUrl(createShortUrlReq);
        //3.获取短链
        String surl = createShortUrlResp.getSurl();
        //4.填充短链
        String msg = msgBody.replace("#", surl);
        //5.发送短信
        if (!StringUtils.isEmpty(email)) {
            if (MessageConstant.SEND_IN_TIME.equals(sendType)) {//立即发送
                concurrentJobExecutor.runAsync(() -> {
                    messageService.sendMessageByEmail(subject, msg, email);
                });
            } else {//定时发送
                dynamicTaskService.scheduleTask(() -> messageService.sendMessageByEmail(subject, msg, email),
                        sendTime);
            }

        }
        if (!StringUtils.isEmpty(phone)) {
            if (MessageConstant.SEND_IN_TIME.equals(sendType)) {
                concurrentJobExecutor.runAsync(() -> {
                    messageService.sendMessageByPhone(subject, msg, phone);
                });
            } else {//定时发送
                dynamicTaskService.scheduleTask(() -> messageService.sendMessageByEmail(subject, msg, phone),
                        sendTime);
            }
        }
    }

    /**
     * 批量发送消息
     */
    @Override
    public void batchSendMessage(BatchSendMessageReq batchSendMessageReq) {
        //1.获取参数
        List<String> phoneList = batchSendMessageReq.getPhoneList();
        List<String> emailList = batchSendMessageReq.getEmailList();
        LocalDateTime sendTime = batchSendMessageReq.getSendTime();
        String sendType = batchSendMessageReq.getSendType();
        String subject = batchSendMessageReq.getSubject();
        String msgBody = batchSendMessageReq.getMsgBody();
        //2.为长链创建短链，需要创建短链请求对象
        CreateShortUrlReq createShortUrlReq = CreateShortUrlReq
                .builder()
                .domain(batchSendMessageReq.getDomain())
                .lurl(batchSendMessageReq.getLurl())
                .groupId(batchSendMessageReq.getGroupId())
                .validTime(batchSendMessageReq.getValidTime())
                .build();
        CreateShortUrlResp createShortUrlResp = createShortUrl(createShortUrlReq);
        //3.获取短链
        String surl = createShortUrlResp.getSurl();
        //4.填充消息体
        String text = msgBody.replace("#", surl);
        //5.批量发送信息（异步）
        if (!CollectionUtils.isEmpty(emailList)) {
            if (MessageConstant.SEND_IN_TIME.equals(sendType)) {
                concurrentJobExecutor.runAsync(() -> {
                    messageService.batchSendMessageByEmail(subject, text, emailList);
                });
            } else {
                dynamicTaskService.scheduleTask(() -> {
                    messageService.batchSendMessageByEmail(subject, text, emailList);
                }, sendTime);
            }
        }
        if (!CollectionUtils.isEmpty(phoneList)) {
            if (MessageConstant.SEND_IN_TIME.equals(sendType)) {
                concurrentJobExecutor.runAsync(() -> {
                    messageService.batchSendMessageByPhone(subject, text, phoneList);
                });
            } else {
                dynamicTaskService.scheduleTask(() -> {
                    messageService.batchSendMessageByEmail(subject, text, phoneList);
                }, sendTime);
            }
        }
    }

    /**
     * 根据长链接查询ShortUrl
     */
    @Override
    public ShortUrl getShortUrlByLurl(String lurl) {
        return baseMapper.selectOne(new LambdaQueryWrapper<ShortUrl>()
                .eq(ShortUrl::getLurl, lurl)
                .ge(ShortUrl::getValidTime, LocalDateTime.now())
                .eq(ShortUrl::getIsDeleted, 0));
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
