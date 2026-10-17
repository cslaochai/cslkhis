package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 知识文档向量索引重建任务状态枚举（1-进行中 2-完成 3-失败）。
 * <p>
 * 任务态只存进程内存，应用重启后失效，前端轮询不到时提示重试。
 */
@Getter
public enum KnowledgeIndexRebuildStatusEnum {

    RUNNING(1, "进行中"),
    DONE(2, "完成"),
    FAILED(3, "失败");

    private final int code;
    private final String label;

    KnowledgeIndexRebuildStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static KnowledgeIndexRebuildStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (KnowledgeIndexRebuildStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        KnowledgeIndexRebuildStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        KnowledgeIndexRebuildStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        KnowledgeIndexRebuildStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
