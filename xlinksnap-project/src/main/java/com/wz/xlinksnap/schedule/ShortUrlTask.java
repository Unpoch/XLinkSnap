package com.wz.xlinksnap.schedule;

import com.wz.xlinksnap.model.entity.ShortUrl;
import com.wz.xlinksnap.service.ShortUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 短链定时任务类
 */
@Component
public class ShortUrlTask {

    @Autowired
    private ShortUrlService shortUrlService;

    /**
     * 定时逻辑删除过期的短链
     * TODO: 布隆过滤器和缓存也要删除
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void logicDeletedExpiredShortUrl() {
        //1.查询所有已过期且未被删除的短链
        List<ShortUrl> expiredShortUrlList = shortUrlService.getAllExpiredShortUrl(LocalDateTime.now());
        //2.对这些短链进行逻辑删除
        expiredShortUrlList.forEach(shortUrl -> {
            shortUrl.setIsDeleted(1);//逻辑删除
        });
        //3.批量更新数据库
        shortUrlService.batchUpdateShortUrl(expiredShortUrlList);
    }
}
