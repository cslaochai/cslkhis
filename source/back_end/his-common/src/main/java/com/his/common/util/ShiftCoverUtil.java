package com.his.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalTime;

/**
 * 班次时段判定 —— 「此刻谁在岗」的唯一实现。
 *
 * <p>排班表的起止时间在库里是<b>字符串</b>（排班信息的开始时间列 / 结束时间列，
 * 形如 "08:00" 或 "08:00:00"），而且存在<b>跨零点夜班</b>（20:00-08:00，start > end）。
 * 因此「now 落在不落在这一班里」不能用简单的 {@code start <= now < end} 判断，
 * 跨零点班次必须拆成「当日 20:00~24:00」或「次日 00:00~08:00」两段。
 *
 * <p><b>为什么抽出来</b>：这套判定在急诊派单（pickOnDutySchedule）、交班班次名（currentShiftName）
 * 里各写了一遍私有实现，本次排班多岗位闭环又要加「今日在岗」和「分诊当班护士」两个消费点。
 * 三份实现各自演化的结果是同一时刻算出不同的在岗名单 —— 所以收成一处，谁要用都调这里。
 *
 * <p><b>解析不了就当不在岗</b>，绝不猜：时间字符串脏了就返回 null，调用方跳过这一条。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShiftCoverUtil {

    /**
     * 解析排班时间字符串（"08:00" / "08:00:00" / "8:00" 补零后）。
     *
     * @return 解析成功返回 {@link LocalTime}；入参为空或格式非法返回 {@code null}
     */
    public static LocalTime parseShiftTime(String text) {
        if (!StringUtils.hasText(text)) {
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
