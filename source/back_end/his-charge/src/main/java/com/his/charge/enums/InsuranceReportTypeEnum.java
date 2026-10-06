package com.his.charge.enums;

import lombok.Getter;

/**
 * 医保报盘报文类型（biz_insurance_report.report_type）。
 *
 * <p>注意：与 biz_report.report_type（检查/检验）语义<b>不同</b>，这里是<b>报文维度</b>（上报/撤销），
 * 不要混用 {@code com.his.medicaltech.enums.ReportTypeEnum}。</p>
 */
@Getter
public enum InsuranceReportTypeEnum {

    UPLOAD(1, "上报报文"),
    CANCEL(2, "撤销报文");

    private final Integer code;
    private final String desc;

    InsuranceReportTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InsuranceReportTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InsuranceReportTypeEnum e : values()) {
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
        InsuranceReportTypeEnum item = getByCode(code);
        return item == null ? "" : item.getDesc();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        InsuranceReportTypeEnum item = getByCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.getDesc();
    }
}
