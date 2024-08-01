package com.wz.xlinksnap.service.impl;

import com.wz.xlinksnap.common.constant.RedisConstant;
import com.wz.xlinksnap.model.metrics.DailyMetrics;
import com.wz.xlinksnap.service.MetricsService;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * PV,UV,VV,IP 统计服务类
 */
@Service
public class MetricsServiceImpl implements MetricsService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置每日指标
     * 统计方式的设计暂时如下
     * PV：每次请求就 + 1
     * UV：利用有效期为一天cookie进行判断
     * VV：利用有效期为30分钟的cookie进行判断
     * IP：利用IP唯一性进行判断
     *
     * TODO：可以放在本地缓存中计数，频繁操作redis带来网络IO损耗
     *       但是本地缓存就要保证一致性问题（多实例缓存不一致）
     */
    @Override
    public void setDailyMetrics(String suffix, HttpServletRequest request, HttpServletResponse response) {
        //0.生成key
        String dailyPVKey = RedisConstant.getMetricsKey(suffix, "pv");
        String dailyUVKey = RedisConstant.getMetricsKey(suffix, "uv");
        String dailyVVKey = RedisConstant.getMetricsKey(suffix, "vv");
        String dailyIPKey = RedisConstant.getMetricsKey(suffix, "ip");
        //1.获取必要参数
        String clientIp = request.getRemoteAddr();
        //2.利用Cookie对UV和VV进行统计
        //2.1 检查并设置UV Cookie，并统计UV
        Cookie[] cookies = request.getCookies();
        boolean uvCookieFound = false;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("uvCookie".equals(cookie.getName())) {
                    uvCookieFound = true;
                    break;
                }
            }
        }
        //如果uvCookie没找到，则创建新的Cookie，并统计UV
        if (!uvCookieFound) {
            // 设置新的UV Cookie
            Cookie uvCookie = new Cookie("uvCookie", "1");
            uvCookie.setMaxAge(24 * 60 * 60); // 设置Cookie有效期为1天
            uvCookie.setPath("/");
            response.addCookie(uvCookie);
            // 新的cookie，认为独立访客，统计UV
            redisTemplate.opsForValue().increment(dailyUVKey);
        }
        //2.2 检查并设置VV Cookie，并统计VV
        Cookie vvCookie = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("vvCookie".equals(cookie.getName())) {
                    vvCookie = cookie;
                    break;
                }
            }
        }
        //Cookie为空或者有效期超过30分钟，创建一个新的Cookie，并计数VV
        if (vvCookie == null || System.currentTimeMillis() - vvCookie.getMaxAge() > 30 * 60 * 1000) {
            // 设置新的VV Cookie
            vvCookie = new Cookie("vvCookie", "1");
            vvCookie.setMaxAge(30 * 60); // 设置Cookie有效期为30分钟
            vvCookie.setPath("/");
            response.addCookie(vvCookie);
            // 统计VV
            redisTemplate.opsForValue().increment(dailyVVKey);
        }
        //3. 统计指标PV，IP
        redisTemplate.opsForValue().increment(dailyPVKey);//PV
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
        // Long dailyUV = redisTemplate.opsForSet().size(dailyUVKey);//UV
        Integer dailyUV = (Integer) redisTemplate.opsForValue().get(dailyUVKey)
        Integer dailyVV = (Integer) redisTemplate.opsForValue().get(dailyVVKey);//VV
        Long dailyIP = redisTemplate.opsForSet().size(dailyIPKey);//IP
        return DailyMetrics.builder()
                .dailyPV(dailyPV)
                .dailyUV(dailyUV)
                .dailyVV(dailyVV)
                .dailyIP(dailyIP == null ? 0 : dailyIP.intValue())
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
