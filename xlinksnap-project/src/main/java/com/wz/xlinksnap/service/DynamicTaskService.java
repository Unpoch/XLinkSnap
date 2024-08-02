package com.wz.xlinksnap.service;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;


public interface DynamicTaskService {

    ScheduledFuture<?> scheduleTask(Runnable task, LocalDateTime startTime);
}
