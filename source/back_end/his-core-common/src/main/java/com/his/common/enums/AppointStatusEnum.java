package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 预约/就诊状态枚举 —— 挂号信息的挂号状态的唯一权威码值。
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
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 还能不能被退号 —— 已挂号(1)、已签到(2)。
     */
    public static boolean isCancelable(Integer code) {
        return code != null && (code == REGISTERED.code || code == CHECKED_IN.code);
    }

    /**
     * 号源还能不能改（改约 / 换号 / 变更医生诊室）—— <b>只允许「已挂号」</b>。
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

    public static AppointStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AppointStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AppointStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AppointStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AppointStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
