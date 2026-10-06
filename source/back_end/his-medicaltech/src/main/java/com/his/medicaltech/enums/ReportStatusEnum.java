package com.his.medicaltech.enums;

import lombok.Getter;

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

    public static ReportStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReportStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看）
     */
    public static String getText(Integer code) {
        ReportStatusEnum item = getByCode(code);
        return item == null ? "" : item.getDesc();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        ReportStatusEnum item = getByCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.getDesc();
    }
}