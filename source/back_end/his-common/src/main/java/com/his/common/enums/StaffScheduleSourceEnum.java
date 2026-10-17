package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static StaffScheduleSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (StaffScheduleSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        StaffScheduleSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        StaffScheduleSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        StaffScheduleSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
