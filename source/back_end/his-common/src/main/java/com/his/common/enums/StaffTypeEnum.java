package com.his.common.enums;

import com.his.common.exception.BusinessException;
import lombok.Getter;

/**
 * 岗位类别枚举
 */
@Getter
public enum StaffTypeEnum {

    /**
     * 医生（含急诊/放射诊断/公卫医师）：唯一有号源的类别
     */
    DOCTOR(1, "医生", true),
    /**
     * 护理（护士、分诊护士、护士长）
     */
    NURSE(2, "护理", false),
    /**
     * 医技（检验技师、检查技师、营养师等）
     */
    MEDICAL_TECH(3, "医技", false),
    /**
     * 药学（药剂师、临床药师）
     */
    PHARMACY(4, "药学", false),
    /**
     * 收费/财务（收费员、医保结算员）
     */
    CASHIER(5, "收费", false),
    /**
     * 行政/其他（管理员、导诊、病案、审计、院领导、患者）
     */
    ADMIN(6, "行政其他", false);

    private final int code;
    private final String label;
    /**
     * 该岗位的排班是否承载号源（决定诊室/挂号费/时间片段/加号/停诊退号是否适用）
     */
    private final boolean source;

    StaffTypeEnum(int code, String label, boolean source) {
        this.code = code;
        this.label = label;
        this.source = source;
    }

    public static StaffTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StaffTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        StaffTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        StaffTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 该岗位类别的排班是否承载号源。null 一律按「无号源」处理：
     * 宁可少一个号源池，也不能让一条岗位不明的排班进挂号下拉。
     */
    public static boolean hasSource(Integer code) {
        StaffTypeEnum type = fromCode(code);
        return type != null && type.isSource();
    }

    public static boolean isDoctor(Integer code) {
        return DOCTOR.code == (code == null ? -1 : code);
    }

    /**
     * 排班写入口的合法性：岗位类别必填且在枚举内。
     */
    public static void assertValid(Integer code) {
        if (fromCode(code) == null) {
            throw new BusinessException("岗位类别不合法：" + code + "（合法值 " + whitelistText() + "）");
        }
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (StaffTypeEnum type : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(type.code).append("-").append(type.label);
        }
        return sb.toString();
    }
}
