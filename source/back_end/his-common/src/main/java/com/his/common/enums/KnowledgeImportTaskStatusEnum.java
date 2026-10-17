package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static KnowledgeImportTaskStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (KnowledgeImportTaskStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        KnowledgeImportTaskStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        KnowledgeImportTaskStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        KnowledgeImportTaskStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
