package com.wz.xlinksnap.controller;


import cn.dev33.satoken.stp.StpUtil;
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
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 长短链接映射表 前端控制器
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-27
 */
@RestController
public class ShortUrlController {

    @Autowired
    private ShortUrlService shortUrlService;

    /**
     * 短链接跳转原始链接
     */
    @GetMapping("/{surl}")
    public void redirect(@PathVariable("surl") String surl, ServletRequest request, ServletResponse response) {
        shortUrlService.redirect(surl, request, response);
    }

    /**
     * TODO：创建短链
     */
    @PostMapping("/api/surl/createShortUrl")
    public Result<CreateShortUrlResp> createShortUrl(@RequestBody CreateShortUrlReq createShortUrlReq) {
        CreateShortUrlResp createShortUrlResp = shortUrlService.createShortUrl(createShortUrlReq);
        return Result.success(createShortUrlResp);
    }


    /**
     * TODO：批量创建短链接
     */
    @PostMapping("/api/surl/batchCreateShortUrl")
    public Result<BatchCreateShortUrlResp> batchCreateShortUrl(@RequestBody
                                                               BatchCreateShortUrlReq batchCreateShortUrlReq) {
        BatchCreateShortUrlResp batchCreateShortUrlResp =
                shortUrlService.batchCreateShortUrl(batchCreateShortUrlReq);
        return Result.success(batchCreateShortUrlResp);
    }

    /**
     * TODO：分页查询短链
     */
    @GetMapping("/api/surl/pageShortUrl")
    public Result<PageShortUrlResp<ShortUrl>> pageShortUrl(PageShortUrlReq pageShortUrlReq) {
        // pageShortUrlReq.setUserId(SaToken获取UserId)？是否需要userId？
        PageShortUrlResp<ShortUrl> pageShortUrlResp = shortUrlService.pageShortUrl(pageShortUrlReq);
        return Result.success(pageShortUrlResp);
    }

    /**
     * 查询分组下所有短链数量
     */
    @GetMapping("/api/surl/queryGroupShortUrlCount")
    public Result<List<QueryGroupShortUrlCountResp>> queryGroupShortUrlCount(@RequestParam
                                                                             QueryGroupShortUrlCountReq queryGroupShortUrlCountReq) {
        // queryGroupShortUrlCountReq.setUserId(token.getUserId);
        List<QueryGroupShortUrlCountResp> result = shortUrlService.queryGroupShortUrlCount(queryGroupShortUrlCountReq);
        return Result.success(result);
    }

    /**
     * 导出短链接excel数据表
     */
    @GetMapping("/api/surl/exportExcel")
    public void exportExcel(HttpServletResponse response) {
        Long userId = StpUtil.getLoginIdAsLong();
        shortUrlService.exportExcel(userId, response);
    }

    /**
     * 分页查询所有已过期短链 和 已删除短链
     */
    @GetMapping("/api/surl/pageExpiredDeletedSurl")
    public Result<PageShortUrlResp<ShortUrl>> pageExpiredDeletedSurl(@RequestParam PageShortUrlReq pageShortUrlReq) {
        PageShortUrlResp<ShortUrl> pageShortUrlResp = shortUrlService.pageExpiredDeletedSurl(pageShortUrlReq);
        return Result.success(pageShortUrlResp);
    }

    /**
     * TODO：短链续期（过期可续，过期才会被删除）
     */
    // @PostMapping("/api/surl/renewalShortUrl")

}

