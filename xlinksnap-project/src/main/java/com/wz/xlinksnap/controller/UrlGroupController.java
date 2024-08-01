package com.wz.xlinksnap.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.api.R;
import com.wz.xlinksnap.common.result.Result;
import com.wz.xlinksnap.model.dto.req.AddUrlGroupReq;
import com.wz.xlinksnap.model.dto.req.QueryGroupShortUrlCountReq;
import com.wz.xlinksnap.model.dto.req.UpdateUrlGroupReq;
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
        addUrlGroupReq.setUserId((Long)StpUtil.getLoginId());
        AddUrlGroupResp addUrlGroupResp = urlGroupService.addUrlGroup(addUrlGroupReq);
        return Result.success(addUrlGroupResp);
    }


    /**
     * 更新短链分组
     */
    @PostMapping("/updateUrlGroup")
    public Result<String> updateUrlGroup(@RequestBody UpdateUrlGroupReq updateUrlGroupReq) {
        updateUrlGroupReq.setUserId((Long)StpUtil.getLoginId());
        urlGroupService.updateUrlGroup(updateUrlGroupReq);
        return Result.success();
    }

    /**
     * TODO：删除短链分组
     * 这里因为数据库表t_url_group没有和短链id做关联，因此后续考虑再做
     * 或者直接删除t_short_url中groupId的短链，然后t_url_group中删除groupId这一行记录
     * 是否需要增加短链的isDeleted字段？进行逻辑删除，方便后续恢复？
     */

}

