package com.wz.xlinksnap.controller;


import com.wz.xlinksnap.common.result.Result;
import com.wz.xlinksnap.model.dto.req.AddUrlGroupReq;
import com.wz.xlinksnap.model.dto.req.QueryGroupShortUrlCountReq;
import com.wz.xlinksnap.model.dto.resp.AddUrlGroupResp;
import com.wz.xlinksnap.model.dto.resp.QueryGroupShortUrlCountResp;
import com.wz.xlinksnap.service.UrlGroupService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 短链分组表 前端控制器
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-30
 */
@RestController
@RequestMapping("/api/group")
public class UrlGroupController {

    @Autowired
    private UrlGroupService urlGroupService;

    /**
     * 创建短链分组
     */
    @PostMapping("/addUrlGroup")
    public Result<AddUrlGroupResp> addUrlGroup(@RequestBody AddUrlGroupReq addUrlGroupReq) {
        //SaToken 获取用户id, set 到 addUrlGroup 中
        AddUrlGroupResp addUrlGroupResp = urlGroupService.addUrlGroup(addUrlGroupReq);
        return Result.success(addUrlGroupResp);
    }



    /**
     * 更新短链分组
     */

    /**
     * 删除短链分组
     */

}

