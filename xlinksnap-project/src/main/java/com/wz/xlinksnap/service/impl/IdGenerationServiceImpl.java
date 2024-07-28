package com.wz.xlinksnap.service.impl;

import com.wz.xlinksnap.common.generation.SnowFlake;
import com.wz.xlinksnap.service.IdGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Id生成服务类
 */
@Service
public class IdGenerationServiceImpl implements IdGenerationService {

    @Autowired
    private SnowFlake snowFlake;

    @Override
    public long generateId() {
        return snowFlake.nextId();
    }
}
