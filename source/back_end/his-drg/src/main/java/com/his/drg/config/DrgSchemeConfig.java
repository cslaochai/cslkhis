package com.his.drg.config;

import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.util.TextUtil;
import com.his.system.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * DRG 结算默认版本配置：系统参数（sys_config）+ Redis 缓存。
 *
 * <p>解析优先级：Redis 缓存（his:drg:default-version） → sys_config 系统参数（drg.default_version）
 * → yml 内建默认（his.drg.default-version）。解析结果回写 Redis，启动时打印实际启用的版本。
 * 运行时可通过 {@link #updateDefaultVersion(String)} 切换，写 sys_config + Redis，下次默认即生效（无需重启）。
 *
 * <p>短码 2.0/3.0 与库内 version 字符串（chs-drg-2.0 / 3.0）的映射见 {@link DrgProperties#resolveDbVersion(String)}。
 */
@Slf4j
@Component
public class DrgSchemeConfig {

    public static final String REDIS_KEY = "his:drg:default-version";

    private final StringRedisTemplate redisTemplate;
    private final SysConfigService sysConfigService;
    private final DrgProperties drgProperties;

    private volatile String shortVersion;
    private volatile String defaultDbVersion;
    private volatile String source;

    public DrgSchemeConfig(StringRedisTemplate redisTemplate, SysConfigService sysConfigService,
                           DrgProperties drgProperties) {
        this.redisTemplate = redisTemplate;
        this.sysConfigService = sysConfigService;
        this.drgProperties = drgProperties;
    }

    @PostConstruct
    public void init() {
        String cached = redisTemplate.opsForValue().get(REDIS_KEY);
        String shortVer;
        if (TextUtil.hasText(cached)) {
            shortVer = cached.trim();
            source = "Redis";
        } else {
            String dbVal = sysConfigService.getConfigValue(SystemConfigKeyConst.DRG_DEFAULT_VERSION, null);
            if (TextUtil.hasText(dbVal)) {
                shortVer = dbVal.trim();
                source = "sys_config";
            } else {
                shortVer = TextUtil.hasText(drgProperties.getDefaultVersion())
                        ? drgProperties.getDefaultVersion().trim() : "3.0";
                source = "yml";
            }
        }

        // 注意：启动期无登录人上下文，禁止向 sys_config 写库（MP 审计填充会取 CurrentUser 而 NPE）。
        // sys_config 行由运营通过切换接口（带登录人）首次设置时落库；启动只把生效值写进 Redis 缓存。

        this.shortVersion = shortVer;
        this.defaultDbVersion = DrgProperties.resolveDbVersion(shortVer);
        redisTemplate.opsForValue().set(REDIS_KEY, shortVer);
        log.info("====== DRG 结算启用版本：{}（库内 version={}）[来源: {}] ======", shortVersion, defaultDbVersion, source);
    }

    /**
     * 当前默认启用的库内 version 字符串（如 chs-drg-2.0 / 3.0）。
     */
    public String getDefaultVersion() {
        return defaultDbVersion;
    }

    /**
     * 当前默认启用的短版本号（2.0 / 3.0）。
     */
    public String getShortVersion() {
        return shortVersion;
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
    public void updateDefaultVersion(String shortVersion) {
        String db = DrgProperties.resolveDbVersion(shortVersion);
        sysConfigService.setConfigValue(SystemConfigKeyConst.DRG_DEFAULT_VERSION, "DRG 结算默认版本", shortVersion.trim());
        redisTemplate.opsForValue().set(REDIS_KEY, shortVersion.trim());
        this.shortVersion = shortVersion.trim();
        this.defaultDbVersion = db;
        this.source = "sys_config";
        log.info("====== DRG 结算版本已切换为：{}（库内 version={}）======", shortVersion, db);
    }
}
