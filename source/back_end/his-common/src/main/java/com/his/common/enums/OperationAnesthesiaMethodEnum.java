package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）。
 */
@Getter
public enum OperationAnesthesiaMethodEnum {

    GENERAL(1, "全身麻醉", "AN001"),
    NEURAXIAL(2, "椎管内麻醉", "AN002"),
    NERVE_BLOCK(3, "神经阻滞麻醉", "AN003"),
    LOCAL(4, "局部麻醉", "AN004"),
    OTHER(5, "其他", "AN005");

    private final int code;
    private final String label;
    /** 该麻醉方式对应的麻醉费项目编码 */
    private final String chargeItemCode;

    OperationAnesthesiaMethodEnum(int code, String label, String chargeItemCode) {
        this.code = code;
        this.label = label;
        this.chargeItemCode = chargeItemCode;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static OperationAnesthesiaMethodEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (OperationAnesthesiaMethodEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        OperationAnesthesiaMethodEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        OperationAnesthesiaMethodEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        OperationAnesthesiaMethodEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
