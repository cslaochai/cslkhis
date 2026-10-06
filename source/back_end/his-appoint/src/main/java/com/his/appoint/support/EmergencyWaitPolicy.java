package com.his.appoint.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.system.entity.SysConfig;
import com.his.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 急诊候诊时限与超时判定。
 *
 * <p><b>为什么分级各有时限：</b>急诊四级分诊的通行口径是「Ⅰ级濒危即刻、Ⅱ级危重 10 分钟、
 * Ⅲ级急症 30 分钟、Ⅳ级非急症 120 分钟」。候诊是否超时不是"红一下就行"的观感问题，
 * 而是"该有人被追问"的触发条件，所以阈值必须按级别分开、且各院可调（放系统参数）。
 *
 * <p><b>判定不落列</b>：超时是入院时间与当下时刻的函数，写成一列就会
 * 出现"定时任务没跑 → 列说没超时、事实已超时"的漂移。这里只在读时算，
 * 登记时刻只快照 {@code target_see_minutes}（当时的要求是多久，属于历史事实）。
 */
@Component
@RequiredArgsConstructor
public class EmergencyWaitPolicy {

    /**
     * 级别 → 兜底时限（分钟），系统参数读不到时用
     */
    private static final Map<Integer, Integer> DEFAULT_MINUTES = Map.of(
            EmergencyTriageRules.LEVEL_CRITICAL, 0,
            EmergencyTriageRules.LEVEL_SEVERE, 10,
            EmergencyTriageRules.LEVEL_URGENT, 30,
            EmergencyTriageRules.LEVEL_NON_URGENT, 120);

    /**
     * 未定级按 Ⅲ级（30 分钟）收口：宁可早报警，不要因为漏填级别而永远不超时
     */
    private static final int DEFAULT_WHEN_NO_LEVEL = 30;

    private static final String CONFIG_KEY_PREFIX = "emergency.wait_deadline_level";

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
                    new LambdaQueryWrapper<SysConfig>().likeRight(SysConfig::getConfigKey, CONFIG_KEY_PREFIX));
            for (SysConfig row : rows) {
                Integer level = levelOfKey(row.getConfigKey());
                if (level != null && StringUtils.hasText(row.getConfigValue())) {
                    cache.put(level, Integer.parseInt(row.getConfigValue().trim()));
                }
            }
        } catch (Exception ignored) {
            // 配置读不到就用默认值：兜底判定不能因为参数表出问题而失效
        }
        cacheLoadedAt.set(now);
    }

    private Integer levelOfKey(String key) {
        if (key == null || !key.startsWith(CONFIG_KEY_PREFIX)) {
            return null;
        }
        try {
            return Integer.parseInt(key.substring(CONFIG_KEY_PREFIX.length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
