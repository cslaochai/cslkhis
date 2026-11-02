package com.his.charge.config;

import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.DrgVersionEnum;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.charge.mapper.DrgSimMapper;
import com.his.charge.vo.DrgSchemeRowVO;
import com.his.spi.service.SystemConfigFacadeService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * DRG 结算默认版本配置：系统参数（sys_config）+ Redis 缓存 + biz_drg_scheme.is_default 兜底。
 *
 * <p>优先级：Redis 缓存 → sys_config → 库内 is_default=1 且启用的方案 → 内置 3.0。
 * 切换版本时必须校验方案存在且 status=1，避免把默认切到未铺底/已停用的版本上。
 */
@Slf4j
@Component
public class DrgSchemeConfig {

    public static final String REDIS_KEY = "his:drg:default-version";

    /**
     * 系统参数与库内都没配置时的内置默认版本：3.0 是当前官方机读规则完整版本
     */
    private static final String DEFAULT_VERSION = DrgVersionEnum.DRG_3_0.getCode();

    private final StringRedisTemplate redisTemplate;
    private final SystemConfigFacadeService sysConfigService;
    private final DrgSimMapper drgSimMapper;

    public DrgSchemeConfig(StringRedisTemplate redisTemplate,
                           SystemConfigFacadeService sysConfigService,
                           DrgSimMapper drgSimMapper) {
        this.redisTemplate = redisTemplate;
        this.sysConfigService = sysConfigService;
        this.drgSimMapper = drgSimMapper;
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
                String schemeDefault = defaultFromSchemeTable();
                if (TextUtil.hasText(schemeDefault)) {
                    version = schemeDefault;
                    source = "biz_drg_scheme.is_default";
                } else {
                    version = DEFAULT_VERSION;
                    source = "内置默认";
                }
            }
        }

        redisTemplate.opsForValue().set(REDIS_KEY, version);
        log.info("====== DRG 结算启用版本：{} [来源: {}] ======", version, source);
    }

    /**
     * 库内默认方案；表还没建（增量 SQL 未执行）时不让启动失败，降级到内置默认
     */
    private String defaultFromSchemeTable() {
        try {
            return drgSimMapper.selectDefaultSchemeVersion();
        } catch (Exception e) {
            log.warn("读取 biz_drg_scheme 默认版本失败，降级到内置默认 {}：{}", DEFAULT_VERSION, e.getMessage());
            return null;
        }
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
     * 运行时切换默认版本：校验方案存在且已启用 → 写库内 is_default + sys_config + Redis。
     */
    public void updateDefaultVersion(String version) {
        String db = DrgVersionEnum.requireValid(version);
        DrgSchemeRowVO scheme = drgSimMapper.scheme(db);
        if (scheme == null) {
            throw new IllegalStateException("DRG 分组方案 " + db + " 不存在，无法设为默认版本");
        }
        if (scheme.getStatus() == null || scheme.getStatus() != 1) {
            throw new IllegalStateException("DRG 分组方案 " + db + " 未启用（status="
                    + scheme.getStatus() + "），无法设为默认版本");
        }
        CurrentUser user = UserUtils.getCurrentUser();
        String operator = user == null ? "system" : user.getRealName();
        Long operatorId = user == null || user.getEmployeeId() == null ? 0L : user.getEmployeeId();
        drgSimMapper.markDefaultScheme(db, operator, operatorId);
        sysConfigService.setConfigValue(SystemConfigKeyConst.DRG_DEFAULT_VERSION, "DRG 结算默认版本", db);
        redisTemplate.opsForValue().set(REDIS_KEY, db);
        log.info("====== DRG 结算版本已切换为：{}（规则来源 {}，verified={}） ======",
                db, scheme.getRuleSource(), scheme.getVerified());
    }
}
