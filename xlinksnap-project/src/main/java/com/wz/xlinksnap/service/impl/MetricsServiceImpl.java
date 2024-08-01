package com.wz.xlinksnap.service.impl;

import com.wz.xlinksnap.common.constant.RedisConstant;
import com.wz.xlinksnap.model.metrics.DailyMetrics;
import com.wz.xlinksnap.service.MetricsService;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * PV,UV,VV,IP 统计服务类
 */
@Service
public class MetricsServiceImpl implements MetricsService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 暂时设计如下
     * 设置每日指标
     * TODO：计算方式存在一些问题
     */
    @Override
    public void setDailyMetrics(String suffix, HttpServletRequest request, HttpServletResponse response) {
        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        //todo:这里存在问题，为空，我们写死（实际上cookie和sessionId一起判断）
        String sessionId = "1";
        // String sessionId = WebUtils.getSessionId(request);
        //key
        String dailyPVKey = RedisConstant.getMetricsKey(suffix, "pv");
        String dailyUVKey = RedisConstant.getMetricsKey(suffix, "uv");
        String dailyVVKey = RedisConstant.getMetricsKey(suffix, "vv");
        String dailyIPKey = RedisConstant.getMetricsKey(suffix, "ip");
        //统计指标
        redisTemplate.opsForValue().increment(dailyPVKey);//PV
        redisTemplate.opsForSet().add(dailyUVKey, sessionId);//UV
        redisTemplate.opsForValue().increment(dailyVVKey);//VV
        redisTemplate.opsForSet().add(dailyIPKey, clientIp);//IP
    }

    /**
     * 获取每日指标
     */
    @Override
    public DailyMetrics getDailyMetrics(String suffix) {
        //key
        String dailyPVKey = RedisConstant.getMetricsKey(suffix, "pv");
        String dailyUVKey = RedisConstant.getMetricsKey(suffix, "uv");
        String dailyVVKey = RedisConstant.getMetricsKey(suffix, "vv");
        String dailyIPKey = RedisConstant.getMetricsKey(suffix, "ip");
        //指标
        Integer dailyPV = (Integer) redisTemplate.opsForValue().get(dailyPVKey);//PV
        Long dailyUV = redisTemplate.opsForSet().size(dailyUVKey);//UV
        Integer dailyVV = (Integer) redisTemplate.opsForValue().get(dailyVVKey);//VV
        Long dailyIP = redisTemplate.opsForSet().size(dailyIPKey);//IP
        return DailyMetrics.builder()
                .dailyPV(dailyPV)
                .dailyUV(dailyUV.intValue())
                .dailyVV(dailyVV)
                .dailyIP(dailyIP.intValue())
                .build();
    }

    /**
     * 删除每日指标的key 对应的value
     */
    @Override
    public void deleteDailyMetricsKey() {
        Set<String> keys = redisTemplate.keys(RedisConstant.DAILY_METRICS);
        assert keys != null;
        redisTemplate.delete(keys);
    }
}
