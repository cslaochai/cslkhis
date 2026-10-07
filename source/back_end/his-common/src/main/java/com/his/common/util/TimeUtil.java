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
}
