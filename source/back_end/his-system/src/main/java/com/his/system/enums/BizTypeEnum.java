package com.his.system.enums;

import lombok.Getter;

/**
 * 站内信业务类型（消息通知.业务类型唯一口径）
 */
@Getter
public enum BizTypeEnum {

    /* ---------- 已接通发送方（3）---------- */
    REGIST("regist", "挂号"),
    APPOINTMENT("appointment", "预约"),
    PRESCRIPTION("prescription", "处方"),
    INSPECTION("inspection", "检查报告"),
    REPORT("report", "检验报告"),
    CRITICAL("critical", "危急值"),
    DRUG("drug", "药品"),

    /* ---------- 门诊域 ---------- */
    APPT_REMIND("appt-remind", "预约提醒"),
    REGIST_REFUND("regist-refund", "退号退费"),
    EMERGENCY_WAIT("emg-wait", "急诊候诊超时"),
    // emg-obs：留观超过系统参数的上限档（通行口径 72 小时）→ 催「定去向」（转住院/离院）。
    // emg-ho：有人交班给自己 → 催「接手」，一次性投递（一次交班一条），不参与重发判重阶梯。
    EMERGENCY_OBS("emg-obs", "急诊留观超时限"),
    EMERGENCY_HANDOVER("emg-ho", "急诊交班接收"),

    /* ---------- 住院域 ---------- */
    ADMIT("admit", "入院收治通知"),
    INPAT_ORDER("inpat-order", "住院医嘱提醒"),
    CONSULT("consult", "会诊邀请"),
    // 死亡证明已开具但过了上报时限 → 催填表医师上报死因监测（按天幂等，同传染病报卡口径）
    DEATH_CERT("death-cert", "死亡证明上报时限"),

    /* ---------- 药房域 ---------- */
    DISPENSE("dispense", "待发药"),
    DRUG_AUDIT("drugaudit", "处方审核结果"),
    DRUG_STOCK("stock", "缺药库存预警"),

    /* ---------- 病历域 ---------- */
    EMR_ARCHIVE("emr-arch", "病历归档超期"),
    EMR_QC("emr-qc", "病历质控问题"),
    ARCHIVE_BORROW("arch-borrow", "病案借阅超期"),
    CODE_TASK("code-task", "编码任务退修"),
    INFECTIOUS_REPORT("infectious-report", "传染病报卡时限预警"),

    /* ---------- 电子签名域 ---------- */
    SIGN("sign", "签名待办提醒"),

    /* ---------- 财务医保域 ---------- */
    SETTLE("settle", "医保结算结果"),
    ARREARS("arrears", "欠费提醒"),

    /* ---------- 系统域 ---------- */
    NOTICE("notice", "系统公告"),
    // duty-coord：发给「当日总值班」的全院协调待办（sql/169）。
    // 触发点三个：①急诊候诊/留观升级阶梯走完仍没人接 ②床位跨科调配 ③双向转诊待确认超时。
    // 收件人是 DutyRosterService.current() 解析出来的那个人，不是科室、不是静态配置。
    DUTY_COORD("duty-coord", "全院协调");

    private final String type;
    private final String desc;

    BizTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }


    public static BizTypeEnum getByType(String type) {
        if (type == null) {
            return null;
        }
        for (BizTypeEnum e : values()) {
            if (e.getType().equals(type)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 紧急度合法值校验（消息通知.severity 口径，见 sql/70）。
     * 发送方传非法值时由 {@code SysMessageServiceImpl} 兜底为 info，不能让脏码值进排序。
     */
    public static boolean isValidSeverity(String severity) {
        return "info".equals(severity) || "warning".equals(severity) || "urgent".equals(severity);
    }
}
