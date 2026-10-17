package com.his.common.enums;

import io.netty.util.internal.StringUtil;
import lombok.Getter;

/**
 * 患者重复档案例档案匹配分级（P5.1 EMPI）
 *
 * <p>本类只表达「码值 ↔ 文案」；判级、成组、理由长度等业务口径在
 * {@code com.his.patient.support.PatientMatchRules}（需要读档案字段，不能放在公共底座里）。
 */
@Getter
public enum PatientMatchLevelEnum {

    /**
     * 1-身份证号相同（强依据，唯一可直接采信的）
     */
    ID_CARD(1, "身份证号相同"),
    /**
     * 2-姓名 + 性别 + 出生日期全等
     */
    NAME_GENDER_BIRTH(2, "姓名+性别+出生日期相同"),
    /**
     * 3-姓名 + 手机号相同
     */
    NAME_PHONE(3, "姓名+手机号相同"),
    /**
     * 4-人工判定（无字段命中，通常是仅同名）
     */
    MANUAL(4, "仅姓名相同（未命中任何强字段）");

    private final int code;
    private final String label;

    PatientMatchLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PatientMatchLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PatientMatchLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 返回label值
     */
    public static String getLabel(Integer code, String defauleValue) {
        PatientMatchLevelEnum item = fromCode(code);
        return item == null ? defauleValue : item.label;
    }

    /**
     * 返回label值
     */
    public static String getLabel(Integer code) {
        PatientMatchLevelEnum item = fromCode(code);
        return item == null ? StringUtil.EMPTY_STRING : item.label;
    }
}
