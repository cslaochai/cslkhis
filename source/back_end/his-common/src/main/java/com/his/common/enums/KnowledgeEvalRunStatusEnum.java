package com.his.common.enums;

import lombok.Getter;

/**
 * 知识库离线评测运行状态枚举（2-完成 3-失败）。
 * <p>
 * 评测同步执行，运行记录只在结束时落库，因此没有「进行中」档位。
 */
@Getter
public enum KnowledgeEvalRunStatusEnum {

    DONE(2, "完成"),
    FAILED(3, "失败");

    private final int code;
    private final String label;

    KnowledgeEvalRunStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static KnowledgeEvalRunStatusEnum fromCode(int code) {
        for (KnowledgeEvalRunStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        KnowledgeEvalRunStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        KnowledgeEvalRunStatusEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
