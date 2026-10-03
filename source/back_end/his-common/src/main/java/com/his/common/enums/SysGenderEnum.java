package com.his.common.enums;

import lombok.Getter;

/**
 * 统一性别枚举（字典性别字典：1-男 2-女 9-未知）
 *
 * <p>2026-09-23 起员工与患者共用同一套性别码值（同一张字典性别字典）：
 * 原员工口径「0-女 1-男」（his_gender_sys）与患者口径「未知=3」（his_patient_gender）已废除，
 * 存量迁移见 sql/75-性别字典统一性别字典与启用状态收口.sql
 * （员工 0→2，患者未知 3→9）。9 取 GB/T 2261.1 的「未说明」档位。
 *
 * <p>文案出口单点在 {@code com.his.patient.support.PatientGenderText}（依赖 his-patient
 * 的模块请继续走它，不要在本枚举之外再散落三元表达式）。
 */
@Getter
public enum SysGenderEnum {

    MALE(1, "男"),
    FEMALE(2, "女"),
    UNKNOWN(9, "未知");

    private final int code;
    private final String label;

    SysGenderEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SysGenderEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SysGenderEnum g : values()) {
            if (g.code == code) {
                return g;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
