package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 预问诊量表题型（choice-单选 text-文本）。
 *
 * <p>量表是后端版本化定义后整体下发给患者端的结构化内容，作答只存题目与答案，
 * 所以这两个码值不进任何列，是题面契约里的判别值 —— 患者端按同样的字符串分支渲染，
 * 改值等于改前端契约，只能新增不能改名。
 */
@Getter
public enum PrevisitQuestionTypeEnum {

    CHOICE("choice", "单选"),
    TEXT("text", "文本");

    private final String code;
    private final String label;

    PrevisitQuestionTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static PrevisitQuestionTypeEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (PrevisitQuestionTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        PrevisitQuestionTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        PrevisitQuestionTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        PrevisitQuestionTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
