package com.wz.xlinksnap.schedule;

import com.wz.xlinksnap.common.bloomfilter.ScalableBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 布隆过滤器定时任务类
 */
@Component
public class BloomFilterTask {

    @Autowired
    private ScalableBloomFilter scalableBloomFilter;

    /**
     * 1/2表示 从1月开始，每两个月执行一次
     * 每两个月的1号凌晨2点执行
     * 重建布隆过滤器
     */
    @Scheduled(cron = "0 0 2 1 1/2 ?")
    public void rebuildBloomFilter() {
        scalableBloomFilter.rebuildFilters();
    }
}
