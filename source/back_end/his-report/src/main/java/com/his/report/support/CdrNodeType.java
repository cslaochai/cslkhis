package com.his.report.support;

/**
 * CDR 时间轴的**节点类型**：患者一次来院的三种形态。
 *
 * <p>为什么节点只有三种：CDR 的组织维度是"就诊次"，不是"表"。一次门诊、
 * 一次住院、一次急诊留观，各自是一条独立的轴；把它们混在一起看，临床上是没有意义的。
 */
public enum CdrNodeType {

    /** 门诊就诊次（锚点：就诊次记录，退化时是单张挂号记录，更早是挂号单） */
    OUTPATIENT("OUTPATIENT", "门诊"),

    /** 住院（锚点：入院记录） */
    INPATIENT("INPATIENT", "住院"),

    /** 急诊（锚点：急诊记录） */
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

    CdrNodeType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    /** 未知码值时原样报出，不回落成合法值（否则看页面的人以为数据没问题） */
    public static CdrNodeType parse(String code) {
        for (CdrNodeType t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("未知的 CDR 节点类型：" + code);
    }
}
