package com.wz.xlinksnap.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 短链接导出excel对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ShortUrlExport {

    @ExcelProperty("短链接ID")
    private Long surlId;

    @ExcelProperty("短链")
    private String surl;

    @ExcelProperty("长链")
    private String lurl;

    @ExcelProperty("有效期")
    private LocalDateTime validTime;

    @ExcelProperty("Page View")
    private Integer PV;

    @ExcelProperty("Unique Visitor")
    private Integer UV;

    @ExcelProperty("Visit View")
    private Integer VV;

    @ExcelProperty("IP")
    private Integer IP;
}
