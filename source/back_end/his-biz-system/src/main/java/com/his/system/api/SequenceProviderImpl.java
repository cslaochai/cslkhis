package com.his.system.api;

import com.his.api.service.SequenceProvider;
import com.his.system.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 分布式序列号端口实现（委托 his-system 的 {@code RedisSequenceService}）。
 */
@Service
@RequiredArgsConstructor
public class SequenceProviderImpl implements SequenceProvider {

    private final RedisSequenceService redisSequenceService;

    @Override
    public long next(String module) {
        return redisSequenceService.next(module);
    }
}
