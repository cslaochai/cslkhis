package com.his.appoint.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预约/就诊状态枚举 —— 挂号信息的挂号状态的<b>唯一权威码值</b>。
 *
 * <p>两个「终态补充」（7/8）是日终结转（{@code DayEndSettleService}）落下来的，
 * 用来堵住「昨天的号没人收尾」：以前未签到、已签到未接诊的挂号会永久停在 1/2，
 * 于是「按今天筛」的每个查询都要各自兜一遍，迟早漏。
 *
 * <p>注意别把 7/8 与队列状态混用：7 在 {@link QueueStatusEnum} 里是「已失效」，
 * 在 {@link OpdLogStatusEnum} 里是「未就诊」，三个枚举各自独立成域。
 */
@Getter
@AllArgsConstructor
public enum AppointStatusEnum {

    /**
     * 1-已挂号（已缴费未签到）
     */
    REGISTERED(1, "已挂号"),

    /**
     * 2-已签到（已入队候诊）
     */
    CHECKED_IN(2, "已签到"),

    /**
     * 3-已接诊（就诊中）
     */
    ACCEPTED(3, "已接诊"),

    /**
     * 4-已就诊
     */
    COMPLETED(4, "已就诊"),

    /**
     * 5-已退号
     */
    CANCELLED(5, "已退号"),

    /**
     * 6-已过号
     */
    OVERDUE(6, "已过号"),

    /**
     * 7-爽约：日终结转判定「挂了号/约了号，但当天没到院签到」
     */
    NO_SHOW(7, "爽约"),

    /**
     * 8-未就诊：日终结转判定「到院签到了，但当天没被接诊」
     */
    UNVISITED(8, "未就诊"),

    /**
     * 未知状态 —— <b>纯解析兜底，不是业务状态</b>。
     *
     * <p>{@link #fromCode} 遇到 null 或枚举外的值会返回它，让调用方不必到处判空；
     * 但它<b>不该被当成一个可落库的值</b>：`挂号信息的挂号状态` 里出现 0
     * 只可能是脏数据（实测该列只有 1/2/4/5/6，无 0 也无 NULL）。
     * 所以它不进字典表（见 {@link #isFallback()}），渲染侧命中不到就出「未知(n)」。
     */
    UNKNOWN(0, "未知状态");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String label;

    /**
     * 根据状态码获取对应的枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，若未匹配则返回 UNKNOWN
     */
    public static AppointStatusEnum fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (AppointStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        // 未匹配到有效状态码时，返回 UNKNOWN 而不是 null，增强系统健壮性
        return UNKNOWN;
    }

    /**
     * 文案；不在枚举内返回 null（与 fromCode 的 UNKNOWN 兜底分开，别把脏值说成「未知状态」以外的东西）
     */
    public static String labelOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (AppointStatusEnum status : values()) {
            if (status.code == code) return status.label;
        }
        return null;
    }

    /**
     * 还能不能被退号 —— 已挂号(1)、已签到(2)。
     *
     * <p>已签到也算：签到后才发现不想看了，当天应当能退（钱在收费处退，号在窗口/看板退）。
     * 但 <b>已接诊(3)/已就诊(4) 之后一律锁死</b>：诊疗已经发生，退号等于篡改诊疗事实。
     *
     * <p>⚠ 这只是<b>状态</b>闸门，不含<b>日期</b>闸门：昨天的号若因日终结转没跑还停在 1/2，
     * 光靠本方法会判成「可退」。退号/改约另有「就诊日已过一律拒绝」，见
     * {@code AppointServiceImpl#visitDatePastReason}（前端同口径在 {@code AppointmentsView#isVisitDatePast}）。
     */
    public static boolean isCancelable(Integer code) {
        return code != null && (code == REGISTERED.code || code == CHECKED_IN.code);
    }

    /**
     * 号源还能不能改（改约 / 换号 / 变更医生诊室）—— <b>只允许「已挂号」</b>。
     *
     * <p>已签到(2) 的号源已经被消耗、患者已经在队列里排着，换号源得连带把队列行搬走，
     * 那不是「改约」而是「先退再挂」，不提供一键操作（否则队列号前缀、诊室快照全要跟着重算）。
     * 已接诊(3)/已就诊(4) 是诊疗事实，直接禁止。
     */
    public static boolean isSourceChangeAllowed(Integer code) {
        return code != null && code == REGISTERED.code;
    }

    /**
     * 是否已经是终态（不会再变化）
     */
    public static boolean isFinal(Integer code) {
        return code != null && (code == COMPLETED.code || code == CANCELLED.code
                || code == OVERDUE.code || code == NO_SHOW.code || code == UNVISITED.code);
    }

    /**
     * 是否是「解析兜底」而不是业务码值 —— 字典投影要把它排除。
     *
     * <p>字典是「列上能出现哪些值」的清单，供筛选下拉与展示用。把 UNKNOWN 放进去，
     * 下拉里就会多一个「未知状态」选项，用户勾了它会生成 {@code regist_status = 0} 的查询，
     * 而库里永远不该有这一行 —— 想找脏数据也不该靠这个入口。
     *
     * <p>把这个判断写在枚举里（而不是让自检那边硬编码跳过 0），是为了让它跟着枚举走：
     * 将来谁再加一个兜底档位，只需在这里多判一次，不用去改自检。
     */
    public boolean isFallback() {
        return this == UNKNOWN;
    }
}
