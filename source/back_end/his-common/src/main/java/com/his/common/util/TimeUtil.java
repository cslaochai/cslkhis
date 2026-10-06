package com.his.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 时间戳秒级归一（全库唯一收口点）。
 *
 * <p><b>为什么需要它</b>：本库 1068 个 {@code datetime} 列精度全部为 0（{@code DATETIME_PRECISION = 0}），
 * MySQL 存进去会**四舍五入**到秒；而 Java 的 {@code LocalDateTime.now()} 带纳秒。
 * 于是"刚生成的对象内存值"与"回读到的库值"可能差 1 秒，
 * 凡是要**落库后立刻回读比较**（幂等键、防重放、签名摘要）就必须先归一。
 *
 * <p><b>什么时候不该用</b>：单纯"记个时间"（createTime / updateTime / 操作日志时间）
 * 直接用 {@code LocalDateTime.now()}，库会自动四舍五入，且更该配实体
 * {@code @TableField(fill = ...)} 让 MP 统一填（见 AGENTS.md §17）。
 *
 * <p>2026-10-06 收口：原先 26 个 service 各写一份私有
 * {@code nowSeconds()} / {@code toSeconds()} / {@code seconds()}，共 283 处调用，
 * 同一语义复制 26 份——这不叫收口，且各自改名导致无法统一调整。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtil {

    /**
     * 归一到秒。null 安全（null 进null 出）。
     */
    public static LocalDateTime toSeconds(LocalDateTime time) {
        return time == null ? null : time.truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * 当前时间归一到秒（等价 {@code toSeconds(LocalDateTime.now())}）。
     * 仅用于"落库后要回读比较"的场景；普通时间戳用 {@code LocalDateTime.now()}。
     */
    public static LocalDateTime nowSeconds() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
