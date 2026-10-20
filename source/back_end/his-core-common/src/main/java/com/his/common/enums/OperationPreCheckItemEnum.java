package com.his.common.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 术前核对要点（手术安全核查单的可核对部分）。
 */
@Getter
public enum OperationPreCheckItemEnum {

    IDENTITY(1, "患者身份与手术部位标识已核对", true),
    SURGERY_CONSENT(2, "手术术式与知情同意书已核对", true),
    ANESTHESIA_CONSENT(3, "麻醉方式与麻醉同意书已核对", true),
    ALLERGY(4, "过敏史与术前用药已核对", true),
    BLOOD_IMPLANT(5, "备血、器械与植入物已到位", false),
    IMAGING_LAB(6, "影像资料与化验结果已确认", false);

    private final int code;
    private final String label;
    private final boolean required;

    OperationPreCheckItemEnum(int code, String label, boolean required) {
        this.code = code;
        this.label = label;
        this.required = required;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 必核项码值（1~4 缺任意一项不允许进入「术前核对完成」） */
    public static List<Integer> requiredCodes() {
        List<Integer> list = new ArrayList<>();
        for (OperationPreCheckItemEnum e : values()) {
            if (e.required) {
                list.add(e.code);
            }
        }
        return list;
    }

    /** 全部核对项（码→文案），供前端渲染勾选框 */
    public static Map<Integer, String> all() {
        Map<Integer, String> m = new LinkedHashMap<>();
        for (OperationPreCheckItemEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }

    public static OperationPreCheckItemEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (OperationPreCheckItemEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        OperationPreCheckItemEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        OperationPreCheckItemEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        OperationPreCheckItemEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
