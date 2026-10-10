package com.his.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 班次时段判定 —— 「此刻谁在岗」的唯一实现。
 */
public final class ShiftCoverUtil {

    /**
     * 解析排班时间字符串（"08:00" / "08:00:00" / "8:00" 补零后）。
     *
     * @return 解析成功返回 {@link LocalTime}；入参为空或格式非法返回 {@code null}
     */
    public static LocalTime parseShiftTime(String text) {
        if (!TextUtil.hasText(text)) {
            return null;
        }
        String value = text.trim();
        // 库里混存 "8:00" 与 "08:00"，LocalTime.parse 只认两位小时
        if (value.length() == 4) {
            value = "0" + value;
        }
        try {
            return LocalTime.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 判断时刻 now 是否落在 [start, end) 这个班次内。
     *
     * <p>两种班次形态：
     * <ul>
     *   <li><b>普通班</b>（start &lt;= end，如 08:00-12:00）：now ∈ [start, end)；</li>
     *   <li><b>跨零点夜班</b>（start &gt; end，如 20:00-08:00）：now ∈ [start, 24:00) ∪ [00:00, end)。</li>
     * </ul>
     *
     * @param now   待判定时刻，null 视为不在岗
     * @param start 班次开始，null 或格式非法视为不在岗
     * @param end   班次结束，null 或格式非法视为不在岗
     */
    public static boolean covers(LocalTime now, LocalTime start, LocalTime end) {
        if (now == null || start == null || end == null) {
            return false;
        }
        if (start.compareTo(end) <= 0) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        // 跨零点：now 在起点之后（含），或 now 在终点之前（不含）
        return !now.isBefore(start) || now.isBefore(end);
    }

    /**
     * 便捷方法：直接吃排班表的字符串列。
     */
    public static boolean covers(LocalTime now, String startTime, String endTime) {
        return covers(now, parseShiftTime(startTime), parseShiftTime(endTime));
    }
}
