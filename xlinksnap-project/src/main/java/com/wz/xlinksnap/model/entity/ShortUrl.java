package com.wz.xlinksnap.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 长短链接映射表
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_short_url")
public class ShortUrl extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 短链接id
     */
    private Long surlId;

    /**
     * 短链后缀
     */
    private String suffix;

    /**
     * 完整短链
     */
    private String surl;

    /**
     * 长链
     */
    private String lurl;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 有效期
     */
    private LocalDateTime validTime;


    /**
     * Page View
     */
    @TableField("PV")
    private Integer PV;

    /**
     * Unique Visitor
     */
    @TableField("UV")
    private Integer UV;

    /**
     * Visit View
     */
    @TableField("VV")
    private Integer VV;

    /**
     * IP（访问链接的独立IP数）
     */
    @TableField("IP")
    private Integer IP;

    /**
     * 每日 Page View
     */
    @TableField(exist = false)
    private Integer dailyPV;

    /**
     * 每日 Unique Visitor
     */
    @TableField(exist = false)
    private Integer dailyUV;

    /**
     * 每日 Visit View
     */
    @TableField(exist = false)
    private Integer dailyVV;

    /**
     * 每日 IP（访问链接的独立IP数）
     */
    @TableField(exist = false)
    private Integer dailyIP;

}
