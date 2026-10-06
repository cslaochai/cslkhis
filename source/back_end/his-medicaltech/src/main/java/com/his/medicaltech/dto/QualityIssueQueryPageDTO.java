package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据质量问题清单查询条件（P5.3）。
 *
 * <p>{@code dimension} 与 {@code ruleCode} 都不传时会跑全部规则 —— 在造数环境
 * （几十条主数据）没问题，但生产环境建议至少指定一个维度，
 * 因为这个接口是"按规则逐条查明细再合并"，属于慢查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据质量问题查询条件")
public class QualityIssueQueryPageDTO extends PageParam {

    /**
     * 维度
     */
    @Schema(description = "维度码（COMPLETENESS/CONSISTENCY/TIMELINESS/UNIQUENESS/VALIDITY，可空=全部）")
    private String dimension;

    /**
     * 规则编码
     */
    @Schema(description = "规则编码（可空=该维度全部规则）")
    private String ruleCode;

    @Schema(description = "严重度（1-提示 2-警告 3-严重，可空=全部）")
    private Integer severity;

    /**
     * 关键字
     */
    @Schema(description = "关键字（患者号 / 患者姓名 / 记录单号 / 问题描述）")
    private String keyword;
}
