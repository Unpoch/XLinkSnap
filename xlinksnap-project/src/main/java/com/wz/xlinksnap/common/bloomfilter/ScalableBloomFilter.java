package com.wz.xlinksnap.common.bloomfilter;

import com.wz.xlinksnap.common.constant.RedisConstant;
import com.wz.xlinksnap.common.util.Base64Converter;
import com.wz.xlinksnap.model.entity.ShortUrl;
import com.wz.xlinksnap.service.ShortUrlService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 可扩展的布隆过滤器实现
 * 提供了可重建布隆过滤器的方法：重建方式为，替换最开头的布隆过滤器，将最新的布隆过滤器添加到末尾（循环）
 */
@Component
public class ScalableBloomFilter {

    private final RedissonClient redissonClient;
    private final ShortUrlService shortUrlService;
    private final int initialCapacity = 100000;
    private final double errorRate = 0.01;
    private final List<String> filterNames = new ArrayList<>();
    private final int maxFilterCount = 5; // 最大布隆过滤器数量
    private final String lockKey = "bloom-filter-lock";

    @Autowired
    public ScalableBloomFilter(RedissonClient redissonClient, ShortUrlService shortUrlService) {
        this.redissonClient = redissonClient;
        this.shortUrlService = shortUrlService;
        for (int i = 0; i < maxFilterCount; i++) {
            filterNames.add("bloom-filter-" + i);
        }
        createNewFilter(filterNames.get(0));
    }

    private void createNewFilter(String name) {
        RBloomFilter<String> filter = redissonClient.getBloomFilter(name);
        filter.tryInit((long) initialCapacity * filterNames.size(), errorRate);
    }

    /**
     * 添加key
     */
    public void add(String key) {
        RBloomFilter<String> filter = redissonClient.getBloomFilter(filterNames.get(filterNames.size() - 1));
        if (filter.getSize() >= (long) initialCapacity * filterNames.size()) {
            String newFilterName = filterNames.remove(0); // 移除最旧的过滤器
            createNewFilter(newFilterName);
            filterNames.add(newFilterName); // 添加到列表末尾
        }
        filter.add(key);
    }

    /**
     * 是否存在该key
     */
    public boolean contains(String key) {
        for (String filterName : filterNames) {
            RBloomFilter<String> filter = redissonClient.getBloomFilter(filterName);
            if (filter.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重建布隆过滤器
     * 重建策略为：使用新的覆盖旧的
     */
    public void rebuildFilters() {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            String tempFilterName = filterNames.get(0); // 使用最旧的过滤器名称作为临时名称
            createNewFilter(tempFilterName);
            RBloomFilter<String> tempFilter = redissonClient.getBloomFilter(tempFilterName);

            // 数据迁移,先根据lurl查询对应的短链对象（未过期且未被删除的）
            List<String> lurlKeys = fetchAllKeys();
            List<String> lurlList = lurlKeys.stream().map(Base64Converter::decodeToOriginString).collect(Collectors.toList());
            List<ShortUrl> shortUrlList = shortUrlService.getShortUrlListByLurlList(lurlList);
            //筛选出key后添加到新的布隆过滤器中
            List<String> newLurlKeys = shortUrlList.stream().map(shortUrl -> Base64Converter.encodeToBase64(shortUrl.getLurl())).collect(Collectors.toList());
            newLurlKeys.forEach(tempFilter::add);

            // 替换旧的布隆过滤器
            filterNames.remove(0); // 移除旧的过滤器名称
            filterNames.add(tempFilterName); // 添加新的过滤器名称
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取布隆过滤器中存在的key
     */
    private List<String> fetchAllKeys() {
        // 实现从缓存或数据库中获取所有长链接
        List<String> keys = new ArrayList<>();
        for (String s : redissonClient.getKeys().getKeysByPattern(RedisConstant.LONG_URL_KEY + "*")) {
            keys.add(s);
        }
        return keys;
    }
}
