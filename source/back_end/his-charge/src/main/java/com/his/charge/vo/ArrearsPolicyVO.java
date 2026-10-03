package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 欠费管控策略 VO（单行）。
 */
@Data
public class ArrearsPolicyVO {

    /**
     * 预警线（元）：欠费达线提示，不拦截
     */
    private BigDecimal warnLine;

    /**
     * 停费线（元）：欠费达线拦截择期类新开医嘱
     */
    private BigDecimal stopLine;

    /**
     * 停费管控开关（0-关 1-开）
     */
    private Integer stopEnabled;

    /**
     * 被拦截的医嘱类别（2-检查 3-检验 4-治疗）
     */
    private String stopClasses;

    /**
     * 备注
     */
    private String remark;
}
