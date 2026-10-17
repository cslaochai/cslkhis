package com.his.medicaltech.enums;

/**
 * CDR 时间轴的**节点类型**：患者一次来院的三种形态。
 */
public enum CdrNodeTypeEnum {

    /**
     * 门诊就诊次（锚点：就诊次记录，退化时是单张挂号记录，更早是挂号单）
     */
    OUTPATIENT("OUTPATIENT", "门诊"),

    /**
     * 住院（锚点：入院记录）
     */
    INPATIENT("INPATIENT", "住院"),

    /**
     * 急诊（锚点：急诊记录）
     */
    EMERGENCY("EMERGENCY", "急诊"),

    /**
     * 跨就诊事件（锚点：患者本人）。
     *
     * <p>危急值、病案质控、随访、转诊、公卫上报这些**不属于某一次就诊**的事，
     * 单独成一条轴。硬塞进某次就诊里会让人误以为是那次就诊发生的。
     */
    PATIENT("PATIENT", "跨就诊");

    private final String code;
    private final String text;

    CdrNodeTypeEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    /**
     * 未知码值时原样报出，不回落成合法值（否则看页面的人以为数据没问题）
     */
    public static CdrNodeTypeEnum parse(String code) {
        for (CdrNodeTypeEnum t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("未知的 CDR 节点类型：" + code);
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
