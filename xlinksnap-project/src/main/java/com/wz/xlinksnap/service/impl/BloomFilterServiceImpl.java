package com.wz.xlinksnap.service.impl;

import com.wz.xlinksnap.service.BloomFilterService;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BloomFilterServiceImpl implements BloomFilterService {

    @Autowired
    private RBloomFilter<String> bloomFilter;

    /**
     * 判断是否已存在surl
     */
    @Override
    public boolean containLUrl(String lurl) {
        return bloomFilter.contains(lurl);
    }

    /**
     * 添加lurl到布隆过滤器
     * TODO：扩容？
     */
    @Override
    public void addLUrl(String lurlKey) {
        bloomFilter.add(lurlKey);
    }
}
