package com.wz.xlinksnap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wz.xlinksnap.common.exception.ConditionException;
import com.wz.xlinksnap.mapper.UrlGroupMapper;
import com.wz.xlinksnap.model.dto.req.AddUrlGroupReq;
import com.wz.xlinksnap.model.dto.req.UpdateUrlGroupReq;
import com.wz.xlinksnap.model.dto.resp.AddUrlGroupResp;
import com.wz.xlinksnap.model.entity.UrlGroup;
import com.wz.xlinksnap.service.UrlGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 短链分组表 服务实现类
 * </p>
 *
 * @author unkonwnzz
 * @since 2024-07-30
 */
@Service
public class UrlGroupServiceImpl extends ServiceImpl<UrlGroupMapper, UrlGroup> implements UrlGroupService {

    /**
     * 添加短链分组
     */
    @Override
    public AddUrlGroupResp addUrlGroup(AddUrlGroupReq addUrlGroupReq) {
        Long userId = addUrlGroupReq.getUserId();
        String name = addUrlGroupReq.getName();
        //1.根据userId查询所有分组
        List<UrlGroup> urlGroupList = getUrlGroupListByUserId(userId);
        Set<String> groupNameSet = urlGroupList.stream().map(UrlGroup::getName).collect(Collectors.toSet());
        //2.看分组名称是否冲突
        if (!groupNameSet.isEmpty() && groupNameSet.contains(name)) {
            throw new ConditionException("分组已存在！");
        }
        //3.插入数据库
        UrlGroup urlGroup = new UrlGroup().setName(name)
                .setUserId(userId);
        baseMapper.insert(urlGroup);
        //4.构建响应对象
        return AddUrlGroupResp.builder().groupId(urlGroup.getId()).build();
    }

    /**
     * 根据userId查询用户所有分组
     */
    @Override
    public List<UrlGroup> getUrlGroupListByUserId(Long userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<UrlGroup>()
                .eq(UrlGroup::getUserId, userId));
    }


    /**
     * 根据用户id和分组id集合获取UrlGroup对象
     */
    @Override
    public List<UrlGroup> getUrlGroupListByGroupIdsAndUserId(Long userId, Set<Long> groupIds) {
        return baseMapper.selectList(new LambdaQueryWrapper<UrlGroup>()
                .eq(UrlGroup::getUserId, userId)
                .in(UrlGroup::getId, groupIds));
    }

    /**
     * 更新短链分组
     */
    @Override
    public void updateUrlGroup(UpdateUrlGroupReq updateUrlGroupReq) {
        //1.获取参数
        Long userId = updateUrlGroupReq.getUserId();
        String name = updateUrlGroupReq.getName();
        //2.看是否已存在分组
        List<UrlGroup> urlGroupList = getUrlGroupListByUserId(userId);
        Set<String> nameSet = urlGroupList.stream().map(UrlGroup::getName).collect(Collectors.toSet());
        if(nameSet.contains(name)) {
            throw new ConditionException("分组已存在");
        }
        //3.更新数据库
        UrlGroup urlGroup = new UrlGroup().setUserId(userId)
                .setName(name)
                .setId(updateUrlGroupReq.getGroupId());
        baseMapper.updateById(urlGroup);
    }

    /**
     * 根据userId查询分组id集合
     */
    @Override
    public List<UrlGroup> getGroupIdsByUserId(Long userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<UrlGroup>()
                .eq(UrlGroup::getUserId,userId));
    }
}
