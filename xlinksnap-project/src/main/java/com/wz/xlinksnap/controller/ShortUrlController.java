package com.wz.xlinksnap.controller;


import com.wz.xlinksnap.common.result.Result;
import com.wz.xlinksnap.model.dto.req.CreateShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.CreateShortUrlReq;
import com.wz.xlinksnap.service.ShortUrlService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 长短链接映射表 前端控制器
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-27
 */
@RestController
@RequestMapping("/api/surl")
public class ShortUrlController {

    @Autowired
    private ShortUrlService shortUrlService;

    /**
     * TODO：短链接跳转原始链接
     */
    @GetMapping("/redirect")
    public void redirect(@RequestParam String surl, ServletRequest request, ServletResponse response) {
        shortUrlService.redirect(surl, request, response);
    }

    /**
     * TODO：创建短链
     */
    @PostMapping("/createShortUrl")
    public Result<CreateShortUrlResp> createShortUrl(@RequestBody CreateShortUrlReq createShortUrlReq) {
        CreateShortUrlResp createShortUrlResp = shortUrlService.createShortUrl(createShortUrlReq);
        return Result.success(createShortUrlResp);
    }


    /**
     * TODO：批量创建短链接
     */

    /**
     * TODO：分页查询短链
     */


}

