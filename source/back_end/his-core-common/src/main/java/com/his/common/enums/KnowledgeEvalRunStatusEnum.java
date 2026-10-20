package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static KnowledgeEvalRunStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (KnowledgeEvalRunStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        KnowledgeEvalRunStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        KnowledgeEvalRunStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        KnowledgeEvalRunStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
