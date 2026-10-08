package com.his.operation.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static OperationPreCheckItemEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationPreCheckItemEnum e : values()) {
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

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 {@code ""}，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "";
        }
        OperationPreCheckItemEnum e = fromCode(code);
        return e != null ? e.label : "";
    }

    /**
     * 码值→异常 / 审计文案。null 返回「未知」，脏值返回「未知(n)」保留原始码值。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        OperationPreCheckItemEnum e = fromCode(code);
        return e != null ? e.label : "未知(" + code + ")";
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
}
