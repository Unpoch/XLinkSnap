package com.wz.xlinksnap.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.api.R;
import com.wz.xlinksnap.common.result.Result;
import com.wz.xlinksnap.model.dto.req.BatchCreateShortUrlReq;
import com.wz.xlinksnap.model.dto.req.PageShortUrlReq;
import com.wz.xlinksnap.model.dto.req.QueryGroupShortUrlCountReq;
import com.wz.xlinksnap.model.dto.req.RenewalShortUrlReq;
import com.wz.xlinksnap.model.dto.resp.BatchCreateShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.CreateShortUrlResp;
import com.wz.xlinksnap.model.dto.req.CreateShortUrlReq;
import com.wz.xlinksnap.model.dto.resp.PageShortUrlResp;
import com.wz.xlinksnap.model.dto.resp.QueryGroupShortUrlCountResp;
import com.wz.xlinksnap.model.dto.resp.RenewalShortUrlResp;
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
     * 创建短链
     */
    @PostMapping("/api/surl/createShortUrl")
    public Result<CreateShortUrlResp> createShortUrl(@RequestBody CreateShortUrlReq createShortUrlReq) {
        CreateShortUrlResp createShortUrlResp = shortUrlService.createShortUrl(createShortUrlReq);
        return Result.success(createShortUrlResp);
    }


    /**
     * 批量创建短链接
     */
    @PostMapping("/api/surl/batchCreateShortUrl")
    public Result<BatchCreateShortUrlResp> batchCreateShortUrl(@RequestBody
                                                               BatchCreateShortUrlReq batchCreateShortUrlReq) {
        BatchCreateShortUrlResp batchCreateShortUrlResp =
                shortUrlService.batchCreateShortUrl(batchCreateShortUrlReq);
        return Result.success(batchCreateShortUrlResp);
    }

    /**
     * 分页查询短链
     */
    @GetMapping("/api/surl/pageShortUrl")
    public Result<PageShortUrlResp<ShortUrl>> pageShortUrl(PageShortUrlReq pageShortUrlReq) {
        PageShortUrlResp<ShortUrl> pageShortUrlResp = shortUrlService.pageShortUrl(pageShortUrlReq);
        return Result.success(pageShortUrlResp);
    }

    /**
     * 查询分组下所有短链数量
     */
    @GetMapping("/api/surl/queryGroupShortUrlCount")
    public Result<List<QueryGroupShortUrlCountResp>> queryGroupShortUrlCount(@RequestParam
                                                                             QueryGroupShortUrlCountReq queryGroupShortUrlCountReq) {
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
     * 短链续期（未过期/过期可续，过期才会被删除，删除可恢复）
     * TODO：缓存续期？删除缓存还是更新缓存，还是直接删除
     */
    @PostMapping("/api/surl/renewalShortUrl")
    public Result<RenewalShortUrlResp> renewalShortUrl(@RequestBody RenewalShortUrlReq renewalShortUrlReq) {
        RenewalShortUrlResp renewalShortUrlResp = shortUrlService.renewalShortUrl(renewalShortUrlReq);
        return Result.success(renewalShortUrlResp);
    }
}

