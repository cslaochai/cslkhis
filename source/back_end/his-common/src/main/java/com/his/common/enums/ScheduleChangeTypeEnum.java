package com.his.common.enums;

import lombok.Getter;

/**
 * 排班变更类型枚举（sql/200，字典 his_schedule_change_type）
 */
@Getter
public enum ScheduleChangeTypeEnum {

    /**
     * 换班：两人互换班次
     */
    SWAP(1, "换班"),
    /**
     * 代班：临时换人顶班，原班归属不变
     */
    SUBSTITUTE(2, "代班"),
    /**
     * 停班：整班取消（抽调查封、科室停摆）
     */
    SUSPEND(3, "停班"),
    /**
     * 加号：在已排班上新增号源
     */
    ADD_SOURCE(4, "加号"),
    /**
     * 减号：收回未发出的号源
     */
    REDUCE_SOURCE(5, "减号"),
    /**
     * 出诊变更：改诊室/时段/班别等出诊属性
     */
    CLINIC_CHANGE(6, "出诊变更");

    private final int code;
    private final String label;

    ScheduleChangeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ScheduleChangeTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ScheduleChangeTypeEnum type : values()) {
            if (type.code == code) {
                return type;
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
        ScheduleChangeTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        ScheduleChangeTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (ScheduleChangeTypeEnum type : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(type.code).append("-").append(type.label);
        }
        return sb.toString();
    }
}
