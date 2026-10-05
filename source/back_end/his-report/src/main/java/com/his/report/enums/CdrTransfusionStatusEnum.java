package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：输血流程状态文案（码值口径 = 库列注释，与医技模块 TransfusionStatusEnum 同码同义；
 * 因 his-report 不依赖 his-medicaltech，此处按 CDR 展示口径单列，合并去向见模块遗留说明）。
 */
@Getter
public enum CdrTransfusionStatusEnum {

    PENDING_CROSSMATCH(0, "待配血"),
    CROSSMATCHED(1, "已配血"),
    ISSUED(2, "已发血"),
    INFUSING(3, "输注中"),
    FINISHED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String label;

    CdrTransfusionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrTransfusionStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrTransfusionStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用：null / 脏码值一律返回空串，绝不返回 null、绝不回落合法值 */
    public static String getText(Integer code) {
        CdrTransfusionStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
