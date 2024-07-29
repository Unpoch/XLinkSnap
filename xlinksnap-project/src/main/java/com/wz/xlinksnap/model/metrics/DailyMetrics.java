package com.wz.xlinksnap.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 每日指标
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMetrics {

    private Integer dailyPV;

    private Integer dailyUV;

    private Integer dailyVV;

    private Integer dailyIP;


}
