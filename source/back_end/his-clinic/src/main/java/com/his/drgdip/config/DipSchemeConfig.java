package com.his.drgdip.config;

import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.enums.DipVersionEnum;
import com.his.common.util.TextUtil;
import com.his.system.service.SysConfigService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * DIP 结算默认病种库版本配置：系统参数（sys_config，key dip.default_version）+ Redis 缓存。
 *
 * <p>与 {@link DrgSchemeConfig} 同构：DRG / DIP 各自维护一套默认版本，互不干扰；
 * 住院结算主付费方式（medpay.mode）决定结算走哪套引擎。
 */
@Slf4j
@Component
public class DipSchemeConfig {

    public static final String REDIS_KEY = "his:dip:default-version";

    /**
     * 系统参数未配置时的内置默认版本（库内 version 字符串）
     */
    private static final String DEFAULT_VERSION = DipVersionEnum.DIP_2_0.getCode();

    private final StringRedisTemplate redisTemplate;
    private final SysConfigService sysConfigService;

    public DipSchemeConfig(StringRedisTemplate redisTemplate, SysConfigService sysConfigService) {
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
            String dbVal = sysConfigService.getConfigValue(SystemConfigKeyConst.DIP_DEFAULT_VERSION, null);
            if (TextUtil.hasText(dbVal)) {
                version = dbVal.trim();
                source = "sys_config";
            } else {
                version = DEFAULT_VERSION;
                source = "内置默认";
            }
        }

        redisTemplate.opsForValue().set(REDIS_KEY, version);
        log.info("====== DIP 结算启用病种库版本：{} [来源: {}] ======", version, source);
    }

    public String getCurrentVersion() {
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
        String db = DipVersionEnum.requireValid(version);
        sysConfigService.setConfigValue(SystemConfigKeyConst.DIP_DEFAULT_VERSION, "DIP 病种库默认版本", db);
        redisTemplate.opsForValue().set(REDIS_KEY, db);
        log.info("====== DIP 病种库版本已切换为：{} ======", db);
    }
}
