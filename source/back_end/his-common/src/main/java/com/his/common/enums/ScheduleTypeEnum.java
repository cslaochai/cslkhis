package com.his.common.enums;

import lombok.Getter;

/**
 * 班别枚举（班次字典上的「属于哪个时段」，门诊号源侧按它归类与展示）
 */
@Getter
public enum ScheduleTypeEnum {

    MORNING(1, "上午"),
    AFTERNOON(2, "下午"),
    ALL_DAY(3, "全天"),
    EARLY_MORNING(4, "凌晨"),
    /**
     * 16:00~23:00 这类不跨零点的夜间班（前夜班/大夜班靠班次名区分，班别只到"夜班"一档）
     */
    NIGHT(5, "夜班");

    private final int code;
    private final String label;

    ScheduleTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ScheduleTypeEnum fromCode(int code) {
        for (ScheduleTypeEnum type : values()) {
            if (type.code == code) return type;
        }
        return null;
    }

    public static String getText(Integer code) {
        ScheduleTypeEnum type = code == null ? null : fromCode(code);
        return type == null ? null : type.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        ScheduleTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 报错文案用的白名单串（新增档位时不用再手改三处提示语）
     */
    public static String whitelistText() {
        return java.util.Arrays.stream(values())
                .map(t -> t.code + "-" + t.label)
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
