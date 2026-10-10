package com.his.appoint.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.util.TextUtil;
import com.his.system.entity.SysConfig;
import com.his.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 急诊留观时限（sql/153）。
 */
@Component
@RequiredArgsConstructor
public class EmergencyObservationPolicy {

    /**
     * 留观预警档（小时），系统参数读不到时用
     */
    public static final int DEFAULT_WARN_HOURS = 48;

    /**
     * 留观上限档（小时），系统参数读不到时用
     */
    public static final int DEFAULT_MAX_HOURS = 72;

    private static final long CONFIG_CACHE_TTL_MS = 60_000L;

    private final SysConfigMapper sysConfigMapper;

    private volatile int warnHours = DEFAULT_WARN_HOURS;
    private volatile int maxHours = DEFAULT_MAX_HOURS;
    private volatile long loadedAt = 0L;

    /**
     * 档位文案不带具体小时数：阈值在系统参数里可改，写死的数字会和配置漂移
     */
    public static String levelText(int level) {
        return switch (level) {
            case 1 -> "超预警";
            case 2 -> "超上限";
            default -> "";
        };
    }

    /**
     * 留观预警时限（小时）
     */
    public int warnHours() {
        refreshIfStale();
        return warnHours;
    }

    /**
     * 留观上限时限（小时）
     */
    public int maxHours() {
        refreshIfStale();
        return maxHours;
    }

    private void refreshIfStale() {
        long now = System.currentTimeMillis();
        if (loadedAt != 0L && now - loadedAt <= CONFIG_CACHE_TTL_MS) {
            return;
        }
        try {
            readInto(SystemConfigKeyConst.EMERGENCY_OBSERVATION_WARN_HOURS, v -> warnHours = v);
            readInto(SystemConfigKeyConst.EMERGENCY_OBSERVATION_MAX_HOURS, v -> maxHours = v);
        } catch (Exception ignored) {
            // 参数表读不通时用兜底值继续判定：预警不能因为配置模块出问题而失效
        }
        loadedAt = now;
    }

    private void readInto(String key, java.util.function.IntConsumer setter) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key).last("LIMIT 1"));
        if (config != null && TextUtil.hasText(config.getConfigValue())) {
            // 配成 0 或负数等于"进留观就报警"，榜会被瞬间填满、真该看的那条反而没人看
            setter.accept(Math.max(Integer.parseInt(config.getConfigValue().trim()), 1));
        }
    }

    /**
     * 留观档位：0-未达预警 1-超预警时限 2-超上限时限。
     *
     * <p>{@code max} 取「配置的上限」与「预警档」的<b>大者</b>：管理员把两个数配反
     * （warn=80 / max=72）时，2 档永远不会命中，榜上看着一片干净、实际没人被提醒。
     *
     * @param obsHours 已留观小时数（向下取整）
     */
    public int levelOf(long obsHours) {
        int warn = warnHours();
        int max = Math.max(maxHours(), warn);
        if (obsHours >= max) {
            return 2;
        }
        return obsHours >= warn ? 1 : 0;
    }
}
