package com.his.medicaltech.support;

import com.his.common.exception.BusinessException;
import com.his.medicaltech.entity.BizExamDevice;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 号源网格口径单点：设备的开放时段 → 一串等长格子。
 *
 * <p>两条不易察觉但会出错的规则，集中在这里：
 * <ol>
 *   <li><b>网格锚在开放开始时刻，不锚在墙上整点</b>。1.5T 磁共振 08:30 开门，
 *       第一格就是 08:30-09:00；若按整点切，08:00-08:30 这段还没开门就会长出号源，
 *       约出去是"到点叫不开机"的纠纷。</li>
 *   <li><b>午间休息是断档，不是格子</b>。上午止于 12:00、下午起于 14:30，
 *       中间不存在格子 —— 于是 11:50 开始、时长 30 分钟的预约一定会被拒
 *       （它要跨 12:10 结束，越过了上午最后一格的结束时刻），
 *       而不是让患者躺在机器上午休两小时。</li>
 * </ol>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExamGrid {

    /**
     * "HH:mm" → 当日分钟数；格式不合直接抛业务异常，不静默当 0 点。
     */
    public static int toMin(String hhmm) {
        if (hhmm == null || !hhmm.matches("\\d{2}:\\d{2}")) {
            throw new BusinessException("时间格式应为 HH:mm，收到：" + hhmm);
        }
        int h = Integer.parseInt(hhmm.substring(0, 2));
        int m = Integer.parseInt(hhmm.substring(3, 5));
        if (h > 23 || m > 59) {
            throw new BusinessException("时间超出范围：" + hhmm);
        }
        return h * 60 + m;
    }

    public static String toHHmm(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    /**
     * 设备当日格子：[开始分钟, 结束分钟]，按时间升序（跨上下午连续，中间是断档）。
     *
     * <p>末段按粒度等长切，落不进一个完整格子的零头（如 11:47-12:00）不成格 ——
     * 宁可少一个号，也不要一个"约得上、做不完"的号。
     */
    public static List<int[]> daySlots(BizExamDevice device) {
        int step = device.getSlotMinutes() == null || device.getSlotMinutes() <= 0 ? 30 : device.getSlotMinutes();
        List<int[]> out = new ArrayList<>();
        appendPeriod(out, device.getAmStart(), device.getAmEnd(), step);
        appendPeriod(out, device.getPmStart(), device.getPmEnd(), step);
        return out;
    }

    private static void appendPeriod(List<int[]> out, String start, String end, int step) {
        if (start == null || start.isBlank() || end == null || end.isBlank()) {
            return;
        }
        int s = toMin(start);
        int e = toMin(end);
        if (e <= s) {
            throw new BusinessException("设备开放时段不合法：开始 " + start + " 必须早于结束 " + end);
        }
        for (int cur = s; cur + step <= e; cur += step) {
            out.add(new int[]{cur, cur + step});
        }
    }

    /**
     * 从格子集合里取覆盖 [from, until) 的下标；返回 null 表示越界或落在断档里。
     *
     * <p>覆盖的判定是"格子区间与预约区间相交"，且要求首格正好从 from 开始、
     * 末格必须把 until 包住 —— 断档（午休）会直接落在"找不到下一格"上。
     */
    public static int[] spanOf(List<int[]> slots, int from, int until) {
        int first = -1;
        int last = -1;
        for (int i = 0; i < slots.size(); i++) {
            int[] s = slots.get(i);
            if (s[0] == from && first < 0) {
                first = i;
            }
            if (first >= 0 && s[0] < until) {
                last = i;
            }
        }
        if (first < 0 || last < first) {
            return null;
        }
        if (slots.get(last)[1] < until) {
            return null;
        }
        // 断档检查：占用的格子必须在时间上首尾相接，否则中间跨过了午休
        for (int i = first; i < last; i++) {
            if (slots.get(i)[1] != slots.get(i + 1)[0]) {
                return null;
            }
        }
        return new int[]{first, last};
    }
}
