package com.wz.xlinksnap.service;

import com.wz.xlinksnap.model.metrics.DailyMetrics;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 指标服务类
 */
public interface MetricsService {

    void setDailyMetrics(String suffix, HttpServletRequest request, HttpServletResponse response);

    DailyMetrics getDailyMetrics(String suffix);
}
