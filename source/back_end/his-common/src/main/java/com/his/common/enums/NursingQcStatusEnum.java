package com.his.common.enums;

import lombok.Getter;

/**
 * 护理质量检查单状态枚举（sql/168，落在护理质量检查单的状态列）
 *
 * <p>「先草稿后确认」是护理质控的固定流程：现场确认完数据要能改，确认（护士长签字）之后
 * 这一轮的数就冻结进台账，要改必须先退回草稿 —— 否则台账会跟着被悄悄改掉，
 * 而上报出去的数字已经在护理部的月度通报里了。
 * <br>字典权威在本枚举，码值改动必须同步 {@code sql/168} 的 {@code his_nursing_qc_status} 段。
 */
@Getter
public enum NursingQcStatusEnum {

    /** 草稿：可改明细 */
    DRAFT(1, "草稿"),
    /** 已确认：明细冻结，只能退回草稿后再改 */
    CONFIRMED(2, "已确认");

    private final int code;
    private final String label;

    NursingQcStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NursingQcStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NursingQcStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        NursingQcStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }
}
