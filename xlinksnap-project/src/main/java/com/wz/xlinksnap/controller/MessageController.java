package com.wz.xlinksnap.controller;

import com.wz.xlinksnap.common.result.Result;
import com.wz.xlinksnap.model.dto.req.SendMessageReq;
import com.wz.xlinksnap.service.MessageService;
import com.wz.xlinksnap.service.ShortUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发送短信/邮箱 信息控制器
 */
@RestController
@RequestMapping("/api/msg")
public class MessageController {

    @Autowired
    private ShortUrlService shortUrlService;

    /**
     * 根据信息模版，生成短链，填充在模版中，并发送信息给指定用户
     */
    @PostMapping("/sendMessage")
    public Result<String> sendMessage(@RequestBody SendMessageReq sendMessageReq) {
        shortUrlService.sendMessage(sendMessageReq);
        return Result.success();
    }

    /**
     * TODO: 批量发送
     */
}
