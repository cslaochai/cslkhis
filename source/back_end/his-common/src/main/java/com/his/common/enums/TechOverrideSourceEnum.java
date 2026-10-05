package com.his.common.enums;

import lombok.Getter;

/**
 * 技术越权登记的来源单据类型（sql/155，落在越权授权事后登记单的来源类型列）。
 *
 * <p>越权必须挂在具体单据上，否则「他越过权」这句话无法回查到是哪台手术。
 * <br>字典权威在本枚举，码值改动必须同步 {@code sql/155} 的 {@code his_tech_override_source} 段。
 */
@Getter
public enum TechOverrideSourceEnum {

    /**
     * 手术申请单
     */
    OPERATION_APPLY(1, "手术申请"),
    /**
     * 日间手术登记单
     */
    DAY_SURGERY(2, "日间手术"),
    /**
     * 住院医嘱主表（医嘱类别 6=手术医嘱）
     */
    INPATIENT_ORDER(3, "住院医嘱"),
    /**
     * 内镜检查记录
     */
    ENDOSCOPY(4, "内镜记录");

    private final int code;
    private final String label;

    TechOverrideSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TechOverrideSourceEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TechOverrideSourceEnum source : values()) {
            if (source.code == code) {
                return source;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        TechOverrideSourceEnum source = fromCode(code);
        return source == null ? "未知(" + code + ")" : source.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        TechOverrideSourceEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
