package com.wz.xlinksnap.model.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 批量发送消息请求对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchSendMessageReq extends CreateShortUrlReq {


    /**
     * 手机号
     */
    private List<String> phoneList;

    /**
     * 邮箱
     */
    private List<String> emailList;

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
     * 1 定时发送
     */
    private String sendType;

    /**
     * 定时发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime sendTime;
}
