package com.his.charge.config;

import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.util.TextUtil;
import com.his.common.enums.DrgVersionEnum;
import com.his.spi.service.SystemConfigFacadeService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * DRG 结算默认版本配置：系统参数（sys_config）+ Redis 缓存。
 */
@Slf4j
@Component
public class DrgSchemeConfig {

    public static final String REDIS_KEY = "his:drg:default-version";

    /**
     * 系统参数未配置时的内置默认版本（库内 version 字符串）
     */
    private static final String DEFAULT_VERSION = DrgVersionEnum.CHS_DRG_2_0.getCode();

    private final StringRedisTemplate redisTemplate;
    private final SystemConfigFacadeService sysConfigService;

    public DrgSchemeConfig(StringRedisTemplate redisTemplate, SystemConfigFacadeService sysConfigService) {
        this.redisTemplate = redisTemplate;
        this.sysConfigService = sysConfigService;
    }

    @PostConstruct
    public void init() {
        String cached = redisTemplate.opsForValue().get(REDIS_KEY);
        String version;
        String source;
        if (TextUtil.hasText(cached)) {
            version = cached.trim();
            source = "Redis";
        } else {
            String dbVal = sysConfigService.getConfigValue(SystemConfigKeyConst.DRG_DEFAULT_VERSION, null);
            if (TextUtil.hasText(dbVal)) {
                version = dbVal.trim();
                source = "sys_config";
            } else {
                version = DEFAULT_VERSION;
                source = "内置默认";
            }
        }

        redisTemplate.opsForValue().set(REDIS_KEY, version);
        log.info("====== DRG 结算启用版本：{} [来源: {}] ======", version, source);
    }


    public String getCurrenttVersion() {
        return redisTemplate.opsForValue().get(REDIS_KEY);
    }

    /**
     * 重新从 sys_config 载入并刷新 Redis 缓存（系统参数被改后调用）。
     */
    public void refresh() {
        redisTemplate.delete(REDIS_KEY);
        init();
    }

    /**
     * 运行时切换默认版本：写 sys_config + Redis，下次默认即生效。
     */
    public void updateDefaultVersion(String version) {
        String db = DrgVersionEnum.requireValid(version);
        sysConfigService.setConfigValue(SystemConfigKeyConst.DRG_DEFAULT_VERSION, "DRG 结算默认版本", db);
        redisTemplate.opsForValue().set(REDIS_KEY, db);
        log.info("====== DRG 结算版本已切换为：{} ======", db);
    }
}
