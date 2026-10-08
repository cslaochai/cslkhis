package com.his.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * 时间格式化 pattern 全库唯一收口点（与 TimeUtil 同族：那边管"归一到秒"，这里管"渲染成什么形状"）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateFormats {

    // ==================== 带横杠的可读格式（对外文案、报表、CSV 导出） ====================

    /**
     * {@code 2026-10-07}。日期展示、日期分组键、按日聚合的对外字段都用它。
     *
     * <p><b>不要用它parse</b>：解析请用 {@code LocalDate.parse(text)}（走ISO_LOCAL_DATE），
     * 本类只负责渲染。
     */
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * {@code 2026-10-07 14:30:05}。全库最常见的完整时刻格式，导出 CSV / 日志 / 对外文本用它。
     */
    public static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * {@code 2026-10-07 14:30}。精确到分钟的展示（护理时间、手术时刻、消息时间）。
     */
    public static final DateTimeFormatter DATETIME_MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * {@code 14:30}。一天内的时刻，不带日期（床头屏、值班表、消息气泡）。
     */
    public static final DateTimeFormatter TIME_MINUTE = DateTimeFormatter.ofPattern("HH:mm");

    // ==================== 紧凑格式（仅用于单号/键/目录名，禁止用于展示） ====================

    /**
     * {@code 20261007}。业务单号的日期段、Redis 日序列 key 的日期后缀、单号前缀。
     *
     * <p>只参与<b>标识符构造</b>，不要拿去给人看，也不要用于任何展示字段。
     */
    public static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * {@code 20261007143005}。业务单号的时间段（14 位）。
     *
     * <p>注意单号生成必须遵守 AGENTS.md §17「当天 MAX(序号)+1」，不要拿这个当序号用——
     * 它只保证可读与大致唯一，同一秒内并发仍会撞唯一键。
     */
    public static final DateTimeFormatter COMPACT_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * {@code 20261007143005123}（毫秒）。仅两处用：第三方支付渠道交易流水戳、
     * 质控兜底流水号——需要同一秒内再区分时才用。
     */
    public static final DateTimeFormatter COMPACT_DATETIME_MS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    // ==================== 协议格式（对接外部系统专用，不要用于库内） ====================

    /**
     * {@code 2026-10-07T14:30:05}。仅医保/TSA 通道报文用（对方协议要求带 {@code T}）。
     *
     * <p>库内所有对外文本仍走 {@link #DATETIME}（空格分隔）。
     */
    public static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // ==================== 解析专用（STRICT，格式非法直接抛，不是"渲染"） ====================

    /**
     * {@code uuuuMMdd} + {@link ResolverStyle#STRICT}，用于解析身份证里的出生日期。
     *
     * <p><b>为什么必须是 {@code uuuu} 而不是 {@code yyyy}</b>：STRICT 解析下
     * {@code yyyy} 是「year-of-era」必须配 era 才能解出年份，缺 era 直接抛
     * {@code DateTimeParseException}；{@code uuuu} 才是 proleptic year。
     * <b>为什么必须 STRICT</b>：身份证 {@code 0230110}（2 月 30 日）这类要当场判非法，
     * SMART 模式会把它悄悄规整成 2 月 28 日然后放过。
     *
     * <p>这是全库唯一该用它的地方——换别处解析日期一律用 {@link #DATE} 对应的 ISO 解析。
     */
    public static final DateTimeFormatter STRICT_COMPACT_DATE =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
}