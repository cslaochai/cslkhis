package com.his.patient.enums;

import lombok.Getter;

/**
 * 住院病历/护理文书状态枚举（biz_inpatient_record.record_status：1-草稿 2-已提交 3-已归档，归档后禁改）
 */
@Getter
public enum InpatientRecordStatusEnum {

    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交"),
    ARCHIVED(3, "已归档");

    private final int code;
    private final String label;

    InpatientRecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InpatientRecordStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InpatientRecordStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 病历文书状态：已归档后禁止修改
     */
    public static boolean isArchived(Integer code) {
        return code != null && code == ARCHIVED.code;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」——
     * 脏数据应由数据治理流程修复，而非界面伪装。
     */
    public static String getText(Integer code) {
        InpatientRecordStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，
     * 用于业务异常消息或审计日志，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        InpatientRecordStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
