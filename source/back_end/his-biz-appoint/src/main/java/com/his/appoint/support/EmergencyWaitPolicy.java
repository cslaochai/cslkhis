package com.his.appoint.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.enums.EmergencyTriageLevelEnum;
import com.his.common.util.TextUtil;
import com.his.system.entity.SysConfig;
import com.his.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 急诊候诊时限与超时判定。
 */
@Component
@RequiredArgsConstructor
public class EmergencyWaitPolicy {

    /**
     * 级别 → 兜底时限（分钟），系统参数读不到时用
     */
    private static final Map<Integer, Integer> DEFAULT_MINUTES = Map.of(
            EmergencyTriageLevelEnum.CRITICAL.getCode(), 0,
            EmergencyTriageLevelEnum.SEVERE.getCode(), 10,
            EmergencyTriageLevelEnum.URGENT.getCode(), 30,
            EmergencyTriageLevelEnum.NON_URGENT.getCode(), 120);

    /**
     * 未定级按 Ⅲ级（30 分钟）收口：宁可早报警，不要因为漏填级别而永远不超时
     */
    private static final int DEFAULT_WHEN_NO_LEVEL = 30;

    private static final long CONFIG_CACHE_TTL_MS = 60_000L;

    /**
     * 超过时限多少算「严重超时」：2 倍时限与「时限+30 分钟」取大者，避免 Ⅰ级 0 分钟时 2 倍仍是 0
     */
    private static final int SEVERE_EXTRA_MINUTES = 30;

    private final SysConfigMapper sysConfigMapper;

    private final Map<Integer, Integer> cache = new ConcurrentHashMap<>();
    private final AtomicLong cacheLoadedAt = new AtomicLong(0L);

    /**
     * 超时档位：0-未超时 1-超时 2-严重超时。
     *
     * @param waitMinutes   已候诊分钟数
     * @param targetMinutes 登记时刻快照的时限（null 时按未定级 30 分钟）
     */
    public static int overdueLevelOf(long waitMinutes, Integer targetMinutes) {
        int target = targetMinutes == null ? DEFAULT_WHEN_NO_LEVEL : Math.max(targetMinutes, 0);
        if (waitMinutes <= target) {
            return 0;
        }
        long severeAfter = Math.max(2L * target, (long) target + SEVERE_EXTRA_MINUTES);
        return waitMinutes > severeAfter ? 2 : 1;
    }

    public static String overdueText(int overdueLevel) {
        return switch (overdueLevel) {
            case 1 -> "超时";
            case 2 -> "严重超时";
            default -> "";
        };
    }

    /**
     * 该分诊级别的应接诊时限（分钟）。0 表示"即刻"。
     */
    public int deadlineMinutes(Integer triageLevel) {
        int level = triageLevel == null ? 0 : triageLevel;
        if (level < 1 || level > 4) {
            return DEFAULT_WHEN_NO_LEVEL;
        }
        refreshIfStale();
        return cache.getOrDefault(level, DEFAULT_MINUTES.getOrDefault(level, DEFAULT_WHEN_NO_LEVEL));
    }

    private void refreshIfStale() {
        long now = System.currentTimeMillis();
        if (!cache.isEmpty() && now - cacheLoadedAt.get() <= CONFIG_CACHE_TTL_MS) {
            return;
        }
        try {
            java.util.List<SysConfig> rows = sysConfigMapper.selectList(
                    new LambdaQueryWrapper<SysConfig>().likeRight(SysConfig::getConfigKey, SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX));
            for (SysConfig row : rows) {
                Integer level = levelOfKey(row.getConfigKey());
                if (level != null && TextUtil.hasText(row.getConfigValue())) {
                    cache.put(level, Integer.parseInt(row.getConfigValue().trim()));
                }
            }
        } catch (Exception ignored) {
            // 配置读不到就用默认值：兜底判定不能因为参数表出问题而失效
        }
        cacheLoadedAt.set(now);
    }

    private Integer levelOfKey(String key) {
        if (key == null || !key.startsWith(SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX)) {
            return null;
        }
        try {
            return Integer.parseInt(key.substring(SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX.length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
