package com.wz.xlinksnap.model.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发送信息请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class SendMessageReq extends CreateShortUrlReq{

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 信息标题
     */
    private String subject;

    /**
     * 信息内容
     * '#' 代表要填充的短链接
     */
    private String msgBody;

    /**
     * 发送要求：0 立即发送
     *         1 定时发送
     */
    private String sendType;

    /**
     * 定时发送时间
     */
    private LocalDateTime sendTime;

}
