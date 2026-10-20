package com.his.pay.spi;

import com.his.pay.service.PayRateLimitService;
import com.his.spi.service.PayRateLimitFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 出码与查单节流端口实现：三道闸的缓存读写原样透出。
 */
@Service
@RequiredArgsConstructor
public class PayRateLimitFacadeServiceImpl implements PayRateLimitFacadeService {

    private final PayRateLimitService payRateLimitService;

    @Override
    public Long intervalRetryAfter(String key, int ttlSeconds) {
        return payRateLimitService.intervalRetryAfter(key, ttlSeconds);
    }

    @Override
    public void release(String key) {
        payRateLimitService.release(key);
    }

    @Override
    public void assertQuota(String key, int limit, int windowSeconds, String actionLabel) {
        payRateLimitService.assertQuota(key, limit, windowSeconds, actionLabel);
    }

    @Override
    public boolean allowsChannelQuery(String key, int ttlSeconds) {
        return payRateLimitService.allowsChannelQuery(key, ttlSeconds);
    }
}
