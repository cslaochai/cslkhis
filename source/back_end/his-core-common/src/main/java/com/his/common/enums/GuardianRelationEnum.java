package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 就诊人关系枚举 —— 与字典患者关系字典码值逐字一致
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
        if (null == code) {
            return null;
        }
        for (GuardianRelationEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        GuardianRelationEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        GuardianRelationEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        GuardianRelationEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
