package com.his.clinical.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 科室维度质控指标（看板排名表用）。
 */
@Data
@Schema(description = "科室质控指标")
public class QcDeptIndicatorVO {

    /**
     * 科室名称（门诊与住院都取各自文书上的科室快照）
     */
    private String deptName;

    /**
     * 质控单数
     */
    private Long totalQc;

    /**
     * 通过数
     */
    private Long passQc;

    /**
     * 平均分
     */
    private BigDecimal avgScore;

    /**
     * 通过率（%）
     */
    private BigDecimal passRate;
}
