package com.his.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * 时间工具：秒级归一 + 日边界 + 时长（全库唯一收口点）。
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
     * 当前时间归一到秒。
     */
    public static LocalDateTime nowSeconds() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * 当日 00:00:00。
     */
    public static LocalDateTime dayStart(LocalDate day) {
        return day == null ? null : day.atStartOfDay();
    }

    /**
     * 当日 23:59:59 —— 按日期过滤的右边界。
     */
    public static LocalDateTime dayEnd(LocalDate day) {
        return day == null ? null : day.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * 区间分钟数：如实反映时间倒挂（结束早于开始返回负数），端点缺失返回 null。
     */
    public static Long minutesBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return null;
        }
        return Duration.between(toSeconds(from), toSeconds(to)).toMinutes();
    }

    /**
     * 已耗时分钟数：只认「已经发生」的区间，端点缺失或时间倒挂都算无值。
     */
    public static Long elapsedMinutes(LocalDateTime from, LocalDateTime to) {
        Long minutes = minutesBetween(from, to);
        return minutes == null || minutes < 0 ? null : minutes;
    }

    /**
     * 已耗时整小时数（向下取整）：端点缺失或倒挂都算 0。
     */
    public static long elapsedHours(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(toSeconds(from), toSeconds(to)).toHours());
    }
}
