package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 该类型在一次住院下是否只允许一人。主管与主诊组长是「谁负责」的唯一答案，
     * 协作不是 —— 一次全院会诊可能有好几个协作医生。
     */
    public static boolean exclusive(Integer code) {
        return ATTENDING.code == code || GROUP_LEADER.code == code;
    }

    public static AttendingRelationTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AttendingRelationTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AttendingRelationTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AttendingRelationTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AttendingRelationTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
