package com.his.common.enums;

import lombok.Getter;

/**
 * 排班生成来源枚举（sql/200，字典 his_staff_schedule_source）
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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        StaffScheduleSourceEnum source = fromCode(code);
        return source == null ? "未知(" + code + ")" : source.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        StaffScheduleSourceEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (StaffScheduleSourceEnum source : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(source.code).append("-").append(source.label);
        }
        return sb.toString();
    }
}
