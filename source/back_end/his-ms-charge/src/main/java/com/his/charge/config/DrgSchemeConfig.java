package com.his.charge.config;

import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.enums.DrgVersionEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.charge.mapper.DrgSimMapper;
import com.his.charge.vo.DrgSchemeRowVO;
import com.his.spi.service.SystemConfigFacadeService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * DRG 结算版本配置：Redis 缓存 + 系统参数（sys_config，"医保付费"页保存）。
 *
 * <p><b>代码里不设任何内置默认版本</b>：没有配置就是"未配置"，{@link #getCurrenttVersion()} 返回 null，
 * {@link #requireVersion()} 直接抛业务异常。理由很直白 —— 一旦代码兜底成某个版本，
 * 谁也不知道这笔结算到底跑的是 2.0 还是 3.0，事后无法追溯口径。
 *
 * <p>切换版本时必须校验方案存在且 status=1，避免把结算挂到未铺底/已停用的版本上。
 */
@Slf4j
@Component
public class DrgSchemeConfig {

    public static final String REDIS_KEY = "his:drg:default-version";

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
        String version = null;
        String source = null;
        if (TextUtil.hasText(cached)) {
            version = cached.trim();
            source = "Redis";
        } else {
            String dbVal = sysConfigService.getConfigValue(SystemConfigKeyConst.DRG_DEFAULT_VERSION, null);
            if (TextUtil.hasText(dbVal)) {
                version = dbVal.trim();
                source = "sys_config";
            }
        }

        // Redis 与参数设置都没配置 → 保持"未配置"状态，绝不兜底成某个版本
        if (!TextUtil.hasText(version)) {
            redisTemplate.delete(REDIS_KEY);
            log.error("====== DRG 结算版本未配置：请在「系统管理 → 参数设置 → 医保付费」选择 DRG 分组方案版本；"
                    + "未配置前不做任何入组与结算口径判定 ======");
            return;
        }

        // 配了一个不存在的/已停用的版本同样视为不可用，否则会把结算挂到空目录上
        DrgSchemeRowVO scheme = schemeOrNull(version);
        if (scheme == null || scheme.getStatus() == null || scheme.getStatus() != 1) {
            redisTemplate.delete(REDIS_KEY);
            log.error("====== DRG 结算版本 {} 不存在或未启用（来源: {}），已拒绝作为结算口径 ======", version, source);
            return;
        }

        redisTemplate.opsForValue().set(REDIS_KEY, version);
        log.info("====== DRG 结算启用版本：{} [来源: {}] ======", version, source);
    }

    private DrgSchemeRowVO schemeOrNull(String version) {
        try {
            return drgSimMapper.scheme(version);
        } catch (Exception e) {
            log.warn("读取 DRG 方案头失败（version={}）：{}", version, e.getMessage());
            return null;
        }
    }

    public String getCurrenttVersion() {
        return redisTemplate.opsForValue().get(REDIS_KEY);
    }

    /**
     * 取当前配置的版本，取不到直接抛业务异常。
     *
     * <p>入组 / 落库这类"产生结算口径"的动作必须走这里：宁可报错让配置补上，
     * 也不能悄悄按某个版本算完再落库。
     */
    public String requireVersion() {
        String v = getCurrenttVersion();
        if (!TextUtil.hasText(v)) {
            throw new BusinessException("未配置 DRG 分组方案版本，请先在「系统管理 → 参数设置 → 医保付费」选择版本后再入组");
        }
        return v.trim();
    }

    /**
     * 重新从 sys_config 载入并刷新 Redis 缓存（系统参数被改后调用）。
     */
    public void refresh() {
        redisTemplate.delete(REDIS_KEY);
        init();
    }

    /**
     * 运行时切换默认版本：校验方案存在且已启用 → 写 sys_config + Redis。
     *
     * <p>biz_drg_scheme 只回答"有哪些版本"，不承担"哪版是默认"：默认口径只有参数设置与 Redis 一处来源。
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
        sysConfigService.setConfigValue(SystemConfigKeyConst.DRG_DEFAULT_VERSION, "DRG 结算默认版本", db);
        redisTemplate.opsForValue().set(REDIS_KEY, db);
        log.info("====== DRG 结算版本已切换为：{}（规则来源 {}，verified={}） ======",
                db, scheme.getRuleSource(), scheme.getVerified());
    }
}
