package com.his.common.enums;

import lombok.Getter;

/**
 * 排班生成来源枚举（sql/200，字典 {@code his_staff_schedule_source}）
 *
 * <p>「这一行是谁弄出来的」必须留痕，否则批量操作没法撤销也没法解释：
 * 手工排的、周模板铺的、从上周整周复制的、换班换出来的，四者混在一起后
 * 「模板改了下周会不会跟着变」这种问题无从回答。
 */
@Getter
public enum StaffScheduleSourceEnum {

    /**
     * 手工排班（排班员逐条录入）
     */
    MANUAL(1, "手工"),
    /**
     * 周模板批量生成
     */
    TEMPLATE(2, "模板"),
    /**
     * 从既有周期复制（复制上周/上月）
     */
    COPY(3, "复制周期"),
    /**
     * 换班/代班产生的行
     */
    SWAP(4, "换班");

    private final int code;
    private final String label;

    StaffScheduleSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StaffScheduleSourceEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StaffScheduleSourceEnum source : values()) {
            if (source.code == code) {
                return source;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        StaffScheduleSourceEnum source = fromCode(code);
        return source == null ? "未知(" + code + ")" : source.getLabel();
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (StaffScheduleSourceEnum source : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(source.code).append("-").append(source.label);
        }
        return sb.toString();
    }
}
