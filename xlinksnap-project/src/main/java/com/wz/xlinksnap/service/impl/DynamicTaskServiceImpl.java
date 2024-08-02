package com.wz.xlinksnap.service.impl;

import com.wz.xlinksnap.service.DynamicTaskService;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态定时任务服务类
 */
@Service
@Slf4j
public class DynamicTaskServiceImpl implements DynamicTaskService {

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    /**
     * 从startTime开始执行任务task
     */
    @Override
    public ScheduledFuture<?> scheduleTask(Runnable task, LocalDateTime startTime) {
        return taskScheduler.schedule(task, Instant.from(startTime));
    }
}
