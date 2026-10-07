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
 *
 * <p>2026-10-07 收口：并入「日边界」与「时长」两族，同样是各 service 自写私有副本漂移出来的结果
 * （{@code atStart}/{@code atEnd}/{@code dayStart}/{@code dayEnd} 8 份、
 * {@code minutesBetween}/{@code hoursBetween} 5 份，且负时长有的返回 null 有的返回负数）。
 * <b>时长口径按调用意图分两个方法，不要凭手气挑</b>：
 * 计费/时长统计只认「已发生」的区间用 {@link #elapsedMinutes}；
 * 需要如实反映时间倒挂（如「已等待分钟」把未来时间显示成负数）用 {@link #minutesBetween}。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtil {

    /**
     * 归一到秒。null 安全（null 进null 出）。
     *
     * <p><b>什么时候用它</b>：传入值可能来自前端/库（带小数秒），需要跟列精度对齐时。
     * <b>什么时候用 {@link #nowSeconds()}</b>：只要"此刻" → 用后者，别再套一层本方法
     * （同一语义两种写法，全仓扫描时才收得干净）。
     * <p>含入参的三元形态（本方法包住整个 {@code a != null ? a : now}）必须保持原样
     * ——那是"优先取入参"的语义，不能拆成 {@code nowSeconds()}。
     */
    public static LocalDateTime toSeconds(LocalDateTime time) {
        return time == null ? null : time.truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * 当前时间归一到秒。
     *
     * <p>仅用于"落库后要回读比较"的场景；单纯记时间的 createTime/updateTime
     * 走实体 {@code @TableField(fill = ...)} 让 MP 统一填（见 AGENTS.md §17），
     * 那个位置不需要手动取时间。
     *
     * <p><b>精度跟随列定义</b>：本库 1068 个 datetime 列全为 {@code DATETIME(0)}，
     * MySQL 按 SQL 标准对超出精度的小数秒做四舍五入且<b>不报任何warning</b>
     * （dev.mysql.com 13.2.6）。将来若改列为 {@code DATETIME(3)}，
     * 本方法要同步改成 {@code toMillis} 之类的版本，并同步全仓调用点——
     * 这正是本类作为唯一出口的意义。
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
     *
     * <p><b>不要用 {@code day.atTime(LocalTime.MAX)}（23:59:59.999999999）</b>：本库 datetime 列精度为 0，
     * MySQL 会把它<b>四舍五入进下一天</b>，于是 "≤ 当天" 实际捞到了次日 00:00:00 的行（见 AGENTS.md §3）。
     */
    public static LocalDateTime dayEnd(LocalDate day) {
        return day == null ? null : day.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * 区间分钟数：如实反映时间倒挂（结束早于开始返回负数），端点缺失返回 null。
     *
     * <p>用于「已等待分钟」这类要暴露异常的场景；计费/合规统计请用 {@link #elapsedMinutes}。
     */
    public static Long minutesBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return null;
        }
        return Duration.between(toSeconds(from), toSeconds(to)).toMinutes();
    }

    /**
     * 已耗时分钟数：只认「已经发生」的区间，端点缺失或时间倒挂都算无值。
     *
     * <p>计费与合规统计用这个 —— 把「结束时间早于开始时间」的脏数据折算成负时长，
     * 等于把一条录入错误静默变成一笔负账单。
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
