package com.his.pharmacy.service.impl;

import com.his.pharmacy.service.DrugStockCacheService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.pharmacy.entity.BizDrugStock;
import com.his.pharmacy.mapper.BizDrugStockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 药品库存缓存服务（Redis）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugStockCacheServiceImpl implements DrugStockCacheService {

    private static final String CACHE_PREFIX = "DRUG_STOCK:";
    private static final long CACHE_TTL_HOURS = 24;
    private final StringRedisTemplate stringRedisTemplate;
    private final BizDrugStockMapper drugStockMapper;
    private final ObjectMapper objectMapper;

    /**
     * 获取药品库存（优先从缓存读取）
     */
    public BizDrugStock getStockByDrugId(Long drugId) {
        String key = CACHE_PREFIX + drugId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, BizDrugStock.class);
            }
        } catch (JsonProcessingException e) {
            log.warn("反序列化药品缓存失败, drugId={}", drugId, e);
        }

        // 缓存未命中，查数据库（按drugId字段查，取最早过期的批次）
        // 这里必须按 drug_id 查：曾用主键 id 比对药品ID，于是「给黄芪锁库」实际锁到了
        // 主键恰好等于 500369 的那个别的药品的批次上（错库存 + 本药不锁 = 可用库存永远不减）。
        LambdaQueryWrapper<BizDrugStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDrugStock::getDrugId, drugId)
                .orderByAsc(BizDrugStock::getExpiryDate)
                .last("LIMIT 1");
        BizDrugStock stock = drugStockMapper.selectOne(wrapper);
        if (stock != null) {
            putCache(drugId, stock);
        }
        return stock;
    }

    /**
     * 更新缓存
     */
    public void putCache(Long drugId, BizDrugStock stock) {
        String key = CACHE_PREFIX + drugId;
        try {
            String json = objectMapper.writeValueAsString(stock);
            stringRedisTemplate.opsForValue().set(key, json, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.warn("序列化药品缓存失败, drugId={}", drugId, e);
        }
    }

    /**
     * 清除缓存
     */
    public void evictCache(Long drugId) {
        stringRedisTemplate.delete(CACHE_PREFIX + drugId);
    }

    /**
     * 清除所有药品缓存
     */
    public void evictAllCache() {
        var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }
}
