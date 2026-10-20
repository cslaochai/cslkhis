package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 护理质量检查类别枚举（sql/168，落在护理质控检查项目录的检查类别列）
 */
@Getter
public enum NursingQcCategoryEnum {

    /**
     * 基础护理（8 项 ×12.5 分）
     */
    BASIC_NURSING(1, "基础护理"),
    /**
     * 专科护理（5 项 ×20 分）
     */
    SPECIALTY(2, "专科护理"),
    /**
     * 安全管理（5 项 ×20 分，含跌倒/压疮防范措施，与不良事件互为因果）
     */
    SAFETY(3, "安全管理"),
    /**
     * 护理文书（4 项 ×25 分）
     */
    DOC(4, "护理文书"),
    /**
     * 院感防控（4 项 ×25 分）
     */
    INFECTION(5, "院感防控");

    private final int code;
    private final String label;

    NursingQcCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static NursingQcCategoryEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NursingQcCategoryEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NursingQcCategoryEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NursingQcCategoryEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        NursingQcCategoryEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
