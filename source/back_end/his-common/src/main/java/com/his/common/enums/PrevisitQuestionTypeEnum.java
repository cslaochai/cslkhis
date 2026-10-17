package com.his.common.enums;

import lombok.Getter;

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

    public static PrevisitQuestionTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PrevisitQuestionTypeEnum type : values()) {
            if (type.code.equals(code)) return type;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(String code) {
        PrevisitQuestionTypeEnum type = fromCode(code);
        return type == null ? "" : type.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(String code) {
        PrevisitQuestionTypeEnum type = fromCode(code);
        if (type != null) {
            return type.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
