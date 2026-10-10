package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.util.TextUtil;
import com.his.system.entity.SysDictData;
import com.his.system.mapper.SysDictDataMapper;
import com.his.system.service.DictCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 数据字典Redis缓存服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictCacheServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements CommandLineRunner, DictCacheService {

    private static final String DICT_CACHE_PREFIX = "sys:dict:";

    private static final long CACHE_EXPIRE_HOURS = 24; // 缓存24小时

    private final StringRedisTemplate stringRedisTemplate;

    private final SysDictDataMapper sysDictDataMapper;

    private final ObjectMapper objectMapper;

    /**
     * 系统启动时加载所有字典数据到Redis
     */
    @Override
    public void run(String... args) {
        log.info("开始加载数据字典到Redis缓存...");
        refreshAllDictCache();
        log.info("数据字典缓存加载完成");
    }

    /**
     * 刷新所有字典缓存
     */
    public void refreshAllDictCache() {
        try {
            // 查询所有字典数据
            LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(SysDictData::getDictSort);
            List<SysDictData> allDictData = sysDictDataMapper.selectList(wrapper);

            // 按字典类型分组并缓存
            allDictData.stream()
                    .collect(java.util.stream.Collectors.groupingBy(SysDictData::getDictType))
                    .forEach((dictType, dataList) -> {
                        try {
                            String key = DICT_CACHE_PREFIX + dictType;
                            String json = objectMapper.writeValueAsString(dataList);
                            stringRedisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                        } catch (Exception e) {
                            log.error("缓存字典数据失败: {}", dictType, e);
                        }
                    });

            log.info("已缓存 {} 种字典类型，共 {} 条数据",
                    allDictData.stream().map(SysDictData::getDictType).distinct().count(),
                    allDictData.size());
        } catch (Exception e) {
            log.error("刷新字典缓存失败", e);
        }
    }

    /**
     * 刷新指定字典类型的缓存
     */
    public void refreshDictCache(String dictType) {
        try {
            LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDictData::getDictType, dictType)
                    .orderByAsc(SysDictData::getDictSort);
            List<SysDictData> dataList = sysDictDataMapper.selectList(wrapper);

            String key = DICT_CACHE_PREFIX + dictType;
            String json = objectMapper.writeValueAsString(dataList);
            stringRedisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

            log.info("已刷新字典缓存: {}，共 {} 条数据", dictType, dataList.size());
        } catch (Exception e) {
            log.error("刷新字典缓存失败: {}", dictType, e);
        }
    }

    /**
     * 从缓存获取字典数据，缓存未命中则从数据库加载
     */
    public List<SysDictData> getDictDataByType(String dictType) {
        if (!TextUtil.hasText(dictType)) {
            return Collections.emptyList();
        }

        try {
            String key = DICT_CACHE_PREFIX + dictType;
            String json = stringRedisTemplate.opsForValue().get(key);

            if (json != null) {
                return objectMapper.readValue(json, new TypeReference<List<SysDictData>>() {
                });
            }

            // 缓存未命中，从数据库加载
            LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDictData::getDictType, dictType)
                    .orderByAsc(SysDictData::getDictSort);
            List<SysDictData> dataList = sysDictDataMapper.selectList(wrapper);

            // 写入缓存
            if (!dataList.isEmpty()) {
                json = objectMapper.writeValueAsString(dataList);
                stringRedisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            }

            return dataList;
        } catch (Exception e) {
            log.error("获取字典数据失败: {}", dictType, e);
            // 降级：直接从数据库查询
            LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDictData::getDictType, dictType)
                    .orderByAsc(SysDictData::getDictSort);
            return sysDictDataMapper.selectList(wrapper);
        }
    }

    /**
     * 字典码值 → 文案。取不到一律空串（与枚举 {@code getText} 同口径），不伪造「未知(n)」，也不回落到某个合法文案。
     */
    @Override
    public String getDicDataLabel(String dictType, Object value) {
        if (value == null) {
            return null;
        }
        String v = String.valueOf(value);
        List<SysDictData> list = getDictDataByType(dictType);
        if (list == null || list.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return list.stream()
                .filter(d -> d != null && v.equals(String.valueOf(d.getDictValue())))
                .map(SysDictData::getDictLabel)
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    /**
     * 删除指定字典类型的缓存
     */
    public void removeDictCache(String dictType) {
        String key = DICT_CACHE_PREFIX + dictType;
        stringRedisTemplate.delete(key);
        log.info("已删除字典缓存: {}", dictType);
    }

    /**
     * 清空所有字典缓存
     */
    public void clearAllDictCache() {
        var keys = stringRedisTemplate.keys(DICT_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
            log.info("已清空所有字典缓存，共 {} 个key", keys.size());
        }
    }
}
