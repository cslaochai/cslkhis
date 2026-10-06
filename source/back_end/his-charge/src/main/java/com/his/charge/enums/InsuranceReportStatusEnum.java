package com.his.charge.enums;

import lombok.Getter;

/**
 * 医保报盘报文状态（biz_insurance_report.status）。
 *
 * <p>0-初始（刚生成，未外发）；1-回执成功；2-回执失败；3-已撤销（被 2305 撤销报文撤销）。</p>
 */
@Getter
public enum InsuranceReportStatusEnum {

    INIT(0, "初始"),
    SUCCESS(1, "回执成功"),
    FAIL(2, "回执失败"),
    CANCELLED(3, "已撤销");

    private final Integer code;
    private final String desc;

    InsuranceReportStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InsuranceReportStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InsuranceReportStatusEnum e : values()) {
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
        InsuranceReportStatusEnum item = getByCode(code);
        return item == null ? "" : item.getDesc();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        InsuranceReportStatusEnum item = getByCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.getDesc();
    }
}
