package com.his.appoint.service.impl;

import com.his.appoint.service.DoctorStatusCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 医生接诊状态缓存服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorStatusCacheServiceImpl implements DoctorStatusCacheService {
    private static final String CACHE_PREFIX = "doctor:status:";

    private static final long CACHE_EXPIRE_HOURS = 24;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 生成缓存key
     * 格式: doctor:status:{doctorId}:{yyyy-MM-dd}
     */
    private String generateKey(Long doctorId) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return CACHE_PREFIX + doctorId + ":" + date;
    }

    /**
     * 设置医生接诊状态
     *
     * @param doctorId 医生ID
     * @param status   状态（0-空闲 1-接诊中 2-暂停）
     */
    public void setStatus(Long doctorId, int status) {
        try {
            String key = generateKey(doctorId);
            stringRedisTemplate.opsForValue().set(key, String.valueOf(status), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("设置医生{}状态为{}", doctorId, status);
        } catch (Exception e) {
            log.error("设置医生状态缓存失败: doctorId={}", doctorId, e);
        }
    }

    /**
     * 获取医生接诊状态
     *
     * @param doctorId 医生ID
     * @return 状态（0-空闲 1-接诊中 2-暂停），缓存不存在时返回0（空闲）
     */
    public int getStatus(Long doctorId) {
        try {
            String key = generateKey(doctorId);
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            log.error("获取医生状态缓存失败: doctorId={}", doctorId, e);
        }
        return STATUS_IDLE;
    }

    /**
     * 清除医生接诊状态（就诊完成时调用）
     *
     * @param doctorId 医生ID
     */
    public void clearStatus(Long doctorId) {
        try {
            String key = generateKey(doctorId);
            stringRedisTemplate.delete(key);
            log.debug("清除医生{}状态", doctorId);
        } catch (Exception e) {
            log.error("清除医生状态缓存失败: doctorId={}", doctorId, e);
        }
    }

    /**
     * 医生开始接诊（叫号时调用）
     */
    public void startConsulting(Long doctorId) {
        setStatus(doctorId, STATUS_CONSULTING);
    }

    /**
     * 医生暂停接诊
     */
    public void pauseConsulting(Long doctorId) {
        setStatus(doctorId, STATUS_PAUSED);
    }

    /**
     * 医生结束接诊（完成就诊时调用）
     */
    public void finishConsulting(Long doctorId) {
        clearStatus(doctorId);
    }
}
