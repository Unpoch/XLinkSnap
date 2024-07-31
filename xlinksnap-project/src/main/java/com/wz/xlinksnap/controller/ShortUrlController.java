package com.wz.xlinksnap.controller;


import com.wz.xlinksnap.common.result.Result;
import com.wz.xlinksnap.model.dto.req.BatchCreateShortUrlReq;
import com.wz.xlinksnap.model.dto.req.PageShortUrlReq;
import com.wz.xlinksnap.model.dto.req.QueryGroupShortUrlCountReq;
import com.wz.xlinksnap.model.dto.resp.BatchCreateShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.CreateShortUrlResp;
import com.wz.xlinksnap.model.dto.req.CreateShortUrlReq;
import com.wz.xlinksnap.model.dto.resp.PageShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.QueryGroupShortUrlCountResp;
import com.wz.xlinksnap.model.entity.ShortUrl;
import com.wz.xlinksnap.service.ShortUrlService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

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
    @PostMapping("/batchCreateShortUrl")
    public Result<BatchCreateShortUrlResp> batchCreateShortUrl(@RequestBody
                                                               BatchCreateShortUrlReq batchCreateShortUrlReq) {
        BatchCreateShortUrlResp batchCreateShortUrlResp =
                shortUrlService.batchCreateShortUrl(batchCreateShortUrlReq);
        return Result.success(batchCreateShortUrlResp);
    }

    /**
     * TODO：分页查询短链
     */
    @GetMapping("/pageShortUrl")
    public Result<PageShortUrlResp<ShortUrl>> pageShortUrl(PageShortUrlReq pageShortUrlReq) {
        // pageShortUrlReq.setUserId(SaToken获取UserId)？是否需要userId？
        PageShortUrlResp<ShortUrl> pageShortUrlResp = shortUrlService.pageShortUrl(pageShortUrlReq);
        return Result.success(pageShortUrlResp);
    }

    /**
     * 查询分组下所有短链数量
     */
    @GetMapping("/queryGroupShortUrlCount")
    public Result<List<QueryGroupShortUrlCountResp>> queryGroupShortUrlCount(@RequestParam
                                                                             QueryGroupShortUrlCountReq queryGroupShortUrlCountReq) {
        // queryGroupShortUrlCountReq.setUserId(token.getUserId);
        List<QueryGroupShortUrlCountResp> result = shortUrlService.queryGroupShortUrlCount(queryGroupShortUrlCountReq);
        return Result.success(result);
    }

}

