package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 剂量上限知识新增/修改入参（id 为空=新增）
 */
@Data
public class DoseLimitUpsertDTO {

    /**
     * 主键，新增时为空
     */
    private Long id;

    /**
     * 成分关键字
     */
    @NotBlank(message = "成分不能为空")
    private String component;

    /**
     * 剂量单位（，只有 g/mg/ug 三值可比）
     */
    @NotBlank(message = "请选择剂量单位")
    private String doseUnit;

    /**
     * 单次最大量（为空=本项不判）
     */
    private BigDecimal maxSingleDose;

    /**
     * 每日最大量（为空=本项不判）
     */
    private BigDecimal maxDailyDose;

    /**
     * 口径说明（按什么人群/剂型定的极量）
     */
    private String note;

    /**
     * 状态（1-启用 0-停用），为空按启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
