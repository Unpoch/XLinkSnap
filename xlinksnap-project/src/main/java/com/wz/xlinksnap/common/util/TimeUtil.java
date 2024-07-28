package com.wz.xlinksnap.common.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 时间工具类
 */
public class TimeUtil {

    /**
     * 计算两个时间的差值
     * @param startTime
     * @param endTime
     * @param unit
     * @return
     */
    public static long calculateDifference(LocalDateTime startTime, LocalDateTime endTime, TimeUnit unit) {
        if (startTime == null || endTime == null || unit == null) {
            throw new IllegalArgumentException("startTime, endTime and unit must not be null");
        }

        Duration duration = Duration.between(startTime, endTime);
        switch (unit) {
            case DAYS:
                return duration.toDays();
            case HOURS:
                return duration.toHours();
            case MINUTES:
                return duration.toMinutes();
            case SECONDS:
                return duration.getSeconds();
            case MILLISECONDS:
                return duration.toMillis();
            case MICROSECONDS:
                return duration.toNanos() / 1000;
            case NANOSECONDS:
                return duration.toNanos();
            default:
                throw new IllegalArgumentException("Unsupported TimeUnit: " + unit);
        }
    }
}
