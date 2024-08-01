package com.wz.xlinksnap.schedule;

import com.wz.xlinksnap.model.entity.ShortUrl;
import com.wz.xlinksnap.model.metrics.DailyMetrics;
import com.wz.xlinksnap.service.MetricsService;
import com.wz.xlinksnap.service.ShortUrlService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 指标定时任务类
 */
@Component
public class MetricsTask {

    @Autowired
    private ShortUrlService shortUrlService;

    @Autowired
    private MetricsService metricsService;

    /**
     * 同步每日指标到 总指标统计
     */
    @Scheduled(cron = "0 0 0 * * ？*")
    public void syncDailyMetrics() {
        //1.查询出所有未过期的短链，未删除的（过期短链定时清除）
        List<ShortUrl> allUnexpiredShortUrl = shortUrlService.getAllUnexpiredShortUrl(LocalDateTime.now());
        //2.计算指标，同步
        allUnexpiredShortUrl.forEach(shortUrl -> {
            String suffix = shortUrl.getSuffix();
            DailyMetrics dailyMetrics = metricsService.getDailyMetrics(suffix);
            shortUrl.setPV(shortUrl.getPV() + dailyMetrics.getDailyPV())
                    .setUV(shortUrl.getVV() + dailyMetrics.getDailyUV())
                    .setVV(shortUrl.getVV() + dailyMetrics.getDailyVV())
                    .setIP(shortUrl.getIP() + dailyMetrics.getDailyIP());
        });
        //3.批量更新数据库
        shortUrlService.batchUpdateShortUrl(allUnexpiredShortUrl);
    }
}
