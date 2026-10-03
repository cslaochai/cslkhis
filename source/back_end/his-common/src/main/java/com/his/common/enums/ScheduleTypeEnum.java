package com.his.common.enums;

import lombok.Getter;

/**
 * 班别枚举（班次字典上的「属于哪个时段」，门诊号源侧按它归类与展示）
 *
 * <p><b>班别是班次的属性，不是时间的推导结果</b>：「全天门诊 08:00~17:00」从起止时间推不出「全天」，
 * 「前夜班 16:00~23:00」与「大夜班 23:00~08:00」在班别维度上是同一个「夜班」，靠班次名区分。
 * 所以建班次时必须显式指定班别，缺省时排班展示只能落进「未分类」。
 *
 * <p>放在公共模块是因为班次字典不再只服务门诊排班（sql/200 起值守/通用册也带班别可选），
 * 门诊与护理两侧都要按它渲染时段标签。
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

    public static String labelOf(Integer code) {
        ScheduleTypeEnum type = code == null ? null : fromCode(code);
        return type == null ? null : type.getLabel();
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
