package com.his.platform.service.impl;

import com.his.platform.service.SignPinSessionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 签名 PIN 免输入窗口的 Redis 实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignPinSessionCacheImpl implements SignPinSessionCache {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean isActive(Long empId, Long certId) {
        if (empId == null || certId == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key(empId, certId)));
        } catch (Exception e) {
            // Redis 故障按「窗口不在」处理：多要一次 PIN 确认，不放行未确认的签名
            log.warn("签名PIN免输入窗口读取失败，降级为要求输入PIN empId={} certId={}", empId, certId, e);
            return false;
        }
    }

    @Override
    public void renew(Long empId, Long certId, int ttlMinutes) {
        if (empId == null || certId == null || ttlMinutes <= 0) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(key(empId, certId), String.valueOf(ttlMinutes),
                    Duration.ofMinutes(ttlMinutes));
        } catch (Exception e) {
            // 写窗口失败只是下次还要输 PIN，不影响本次签名结果
            log.warn("签名PIN免输入窗口续期失败 empId={} certId={}", empId, certId, e);
        }
    }

    private String key(Long empId, Long certId) {
        return KEY_PREFIX + empId + ":" + certId;
    }
}
