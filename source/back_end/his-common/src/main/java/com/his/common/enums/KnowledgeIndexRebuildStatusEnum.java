package com.his.common.enums;

import lombok.Getter;

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

    public static KnowledgeIndexRebuildStatusEnum fromCode(int code) {
        for (KnowledgeIndexRebuildStatusEnum item : values()) {
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
        KnowledgeIndexRebuildStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        KnowledgeIndexRebuildStatusEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
