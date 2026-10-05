package com.his.patient.enums;

import lombok.Getter;

/**
 * 就诊人关系枚举 —— 与字典患者关系字典码值逐字一致
 * （1-本人 2-配偶 3-父亲 4-母亲 5-儿子 6-女儿 7-兄弟 8-姐妹 9-祖父 10-祖母
 * 11-外祖父 12-外祖母 13-其他亲属 14-朋友 15-同事 16-单位 99-其他）。
 */
@Getter
public enum GuardianRelationEnum {

    SELF(1, "本人"),
    SPOUSE(2, "配偶"),
    FATHER(3, "父亲"),
    MOTHER(4, "母亲"),
    SON(5, "儿子"),
    DAUGHTER(6, "女儿"),
    BROTHER(7, "兄弟"),
    SISTER(8, "姐妹"),
    GRANDPA(9, "祖父"),
    GRANDMA(10, "祖母"),
    MATERNAL_GRANDPA(11, "外祖父"),
    MATERNAL_GRANDMA(12, "外祖母"),
    OTHER_FAMILY(13, "其他亲属"),
    FRIEND(14, "朋友"),
    COLLEAGUE(15, "同事"),
    COMPANY(16, "单位"),
    OTHER(99, "其他");

    private final int code;
    private final String label;

    GuardianRelationEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static GuardianRelationEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (GuardianRelationEnum r : values()) {
            if (r.code == code) return r;
        }
        return null;
    }

    public static String getText(Integer code) {
        GuardianRelationEnum r = fromCode(code);
        return r == null ? OTHER.label : r.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        GuardianRelationEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
