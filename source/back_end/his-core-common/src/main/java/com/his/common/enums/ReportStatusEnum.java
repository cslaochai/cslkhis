package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum ReportStatusEnum {

    /**
     * 草稿（sql/138 新增）：写了没提交，或审核被退回后回到这个态。
     *
     * <p>原来最小状态是 1-待审核，意味着「医师写到一半」这个真实存在的状态无处安放 ——
     * 报告要么不存在，要么直接进了待审核队列被人审到一份半成品。
     */
    DRAFT(0, "草稿"),
    PENDING_REVIEW(1, "待审核"),
    FIRST_REVIEW_PASS(2, "初审通过"),
    REVIEWED(3, "已审核"),
    PUBLISHED(4, "已发布"),
    INVALID(5, "已作废");

    private final Integer code;
    private final String desc;

    ReportStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static ReportStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ReportStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ReportStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ReportStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        ReportStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}