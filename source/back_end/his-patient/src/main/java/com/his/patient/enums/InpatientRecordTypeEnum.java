package com.his.patient.enums;

import lombok.Getter;

/**
 * 住院病历文书类型枚举
 */
@Getter
public enum InpatientRecordTypeEnum {

    ADMISSION(1, "入院记录"),
    FIRST_COURSE(2, "首次病程"),
    DAILY_COURSE(3, "日常病程"),
    PRE_OP_SUMMARY(4, "术前小结"),
    OPERATION_RECORD(5, "手术记录"),
    POST_OP_FIRST_COURSE(6, "术后首次病程"),
    DISCHARGE_RECORD(7, "出院记录"),
    DEATH_RECORD(8, "死亡记录"),
    CONSULTATION(9, "会诊记录"),
    TRANSFER(10, "转科记录"),
    TRANSFUSION(11, "输血记录");

    private final int code;
    private final String label;

    InpatientRecordTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InpatientRecordTypeEnum fromCode(int code) {
        for (InpatientRecordTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」——
     * 脏数据应由数据治理流程修复，而非界面伪装。
     */
    public static String getText(Integer code) {
        InpatientRecordTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }
    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，
     * 用于业务异常消息或审计日志，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        InpatientRecordTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 只有「入院记录 / 出院记录 / 死亡记录」要求诊断要素必填。
     * <p>日常病程不一定带新诊断，把诊断设成病程的必填项会逼医生写假诊断，
     * 反而污染首页主要诊断的取数。
     */
    public static boolean requiresDiagnosis(Integer code) {
        return code != null && (code == ADMISSION.code || code == DISCHARGE_RECORD.code || code == DEATH_RECORD.code);
    }

    /**
     * 是否为"病程类"文书（正文写在 courseNote）
     */
    public static boolean isCourseRecord(Integer code) {
        return code != null && (code == FIRST_COURSE.code || code == DAILY_COURSE.code || code == POST_OP_FIRST_COURSE.code);
    }

    /**
     * 是否为「会诊记录」（P4.1 由会诊完成时系统回写，走自己的结构化要素清单）
     */
    public static boolean isConsultRecord(Integer code) {
        return code != null && code == CONSULTATION.code;
    }

    /**
     * 是否为「转科记录」（P4.2 由转科接收时系统回写，走自己的结构化要素清单）
     */
    public static boolean isTransferRecord(Integer code) {
        return code != null && code == TRANSFER.code;
    }

    /**
     * 是否为「手术记录」（record_type=5）。
     *
     * <p>与会诊（9）/转科（10）<b>不同</b>：5 这个码值 P2 就有了，医生本来就能手写，
     * 所以它<b>不属于"系统专用文书"</b>（见 {@link #isSystemWritten}，其中不含 5），
     * 手工新增不会被拒。P4.3 只是让手术闭环完成时**也**回写一份。
     */
    public static boolean isOperationRecord(Integer code) {
        return code != null && code == OPERATION_RECORD.code;
    }

    /**
     * 是否为「输血记录」（record_type=11，P4.4 新增码值）。
     *
     * <p>与 5-手术记录<b>不同</b>：5 这个码值 P2 就存在、医生本来就能手写；
     * 11 是本闭环新引入的，因此它是"系统专用"文书（见 {@link #isSystemWritten}），
     * 手工新增会被拒 —— 一条没有血袋、没有配血、没有双人核对的"输血记录"就是假病历，
     * 而输血恰恰是飞检必查、追溯要求最高的一类记录。
     */
    public static boolean isTransfusionRecord(Integer code) {
        return code != null && code == TRANSFUSION.code;
    }

    /**
     * 是否为「系统回写」文书（会诊记录 / 转科记录 / 输血记录）。
     * <p>这些文书不由医生书写，因此：不允许手工新增、不计入"医生该写的文书"，
     * 结构化率走各自的精简要素清单。
     */
    public static boolean isSystemWritten(Integer code) {
        return isConsultRecord(code) || isTransferRecord(code) || isTransfusionRecord(code);
    }
}
