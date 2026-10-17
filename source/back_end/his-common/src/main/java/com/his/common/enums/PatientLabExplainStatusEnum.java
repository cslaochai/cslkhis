package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 患者端检验报告解读的单项状态（暴露在 PatientLabItemPlainVO.status，前端按此渲染箭头与文案）。
 */
@Getter
public enum PatientLabExplainStatusEnum {

    NORMAL(1, "正常"),
    HIGH(2, "偏高"),
    LOW(3, "偏低"),
    UNJUDGED(4, "待核对"),
    ABNORMAL(5, "异常");

    private final Integer code;
    private final String desc;

    PatientLabExplainStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static PatientLabExplainStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PatientLabExplainStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PatientLabExplainStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PatientLabExplainStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        PatientLabExplainStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}
