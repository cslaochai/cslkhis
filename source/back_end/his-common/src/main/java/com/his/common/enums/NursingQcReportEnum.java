package com.his.common.enums;

import lombok.Getter;

/**
 * 护理质量台账上报状态枚举（sql/168，落在护理质控指标台账的上报状态列）
 *
 * <p><b>已上报的行禁止被重算覆盖</b>：报出去的数字进了护理部月度通报和评审资料，
 * 重算按钮一键把它改掉，等于「上月跌倒率 3.5‰ 悄悄变成 7‰」，事后无从解释。
 * 要改必须先退回未上报，改完事实来源（检查单/不良事件）再重算再上报。
 * <br>字典权威在本枚举，码值改动必须同步 {@code sql/168} 的 {@code his_nursing_qc_report} 段。
 */
@Getter
public enum NursingQcReportEnum {

    /**
     * 未上报：可被重算覆盖
     */
    UNREPORTED(1, "未上报"),
    /**
     * 已上报：重算跳过，只能先退回
     */
    REPORTED(2, "已上报");

    private final int code;
    private final String label;

    NursingQcReportEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NursingQcReportEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NursingQcReportEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        NursingQcReportEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }
}
