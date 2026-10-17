package com.his.common.enums;

import lombok.Getter;

/**
 * 知识文档导入任务状态枚举（1-解析中 2-待确认 3-入库中 4-完成 5-失败 6-已取消）。
 * <p>
 * 仅 1/2 可取消 → 6-已取消；任务态只存进程内存，应用重启后失效。
 */
@Getter
public enum KnowledgeImportTaskStatusEnum {

    PARSING(1, "解析中"),
    PENDING_CONFIRM(2, "待确认"),
    COMMITTING(3, "入库中"),
    DONE(4, "完成"),
    FAILED(5, "失败"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String label;

    KnowledgeImportTaskStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static KnowledgeImportTaskStatusEnum fromCode(int code) {
        for (KnowledgeImportTaskStatusEnum item : values()) {
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
        KnowledgeImportTaskStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        KnowledgeImportTaskStatusEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
