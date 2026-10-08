package com.his.common.enums;

import lombok.Getter;

/**
 * 住院管床关系类型枚举（sql/202，字典 his_attending_relation_type）
 */
@Getter
public enum AttendingRelationTypeEnum {

    /**
     * 主管（管床医生）
     */
    ATTENDING(1, "主管"),
    /**
     * 主诊组长
     */
    GROUP_LEADER(2, "主诊组长"),
    /**
     * 协作（会诊/参与治疗）
     */
    ASSIST(3, "协作");

    private final int code;
    private final String label;

    AttendingRelationTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AttendingRelationTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AttendingRelationTypeEnum type : values()) {
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
        AttendingRelationTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        AttendingRelationTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 该类型在一次住院下是否只允许一人。主管与主诊组长是「谁负责」的唯一答案，
     * 协作不是 —— 一次全院会诊可能有好几个协作医生。
     */
    public static boolean exclusive(Integer code) {
        return ATTENDING.code == code || GROUP_LEADER.code == code;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (AttendingRelationTypeEnum type : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(type.code).append("-").append(type.label);
        }
        return sb.toString();
    }
}
