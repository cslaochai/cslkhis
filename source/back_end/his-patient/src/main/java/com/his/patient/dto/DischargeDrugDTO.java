package com.his.patient.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 出院带药 DTO 集合。
 */
public class DischargeDrugDTO {

    /**
     * 开单 / 修改（id 为空=新增；仅「待发药」可改）
     */
    @Data
    public static class Upsert implements Serializable {

        /** 主键（修改时必填） */
        private Long id;

        /** 入院ID */
        @NotNull(message = "入院ID不能为空")
        private Long admissionId;

        /** 药品ID（临时药品手填时可空） */
        private Long drugId;

        /** 药品名称 */
        @NotBlank(message = "药品名称不能为空")
        private String drugName;

        /** 规格 */
        private String spec;

        /** 每次剂量/用法用量描述 */
        private String dosage;

        /** 单位 */
        private String unit;

        /** 带药数量 */
        @NotNull(message = "带药数量不能为空")
        @DecimalMin(value = "0.01", message = "带药数量必须大于 0")
        private BigDecimal quantity;

        /** 用药医嘱（如每日三次饭后） */
        private String usageText;

        /** 用药天数 */
        private Integer days;

        /** 备注 */
        private String remark;
    }

    /**
     * 分页查询
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class QueryPage extends PageParam implements Serializable {

        /** 入院ID */
        private Long admissionId;

        /** 患者ID */
        private Long patientId;

        /** 药品名称模糊 */
        private String drugName;

        /** 发药状态（1-待发药 2-已发药） */
        private Integer dispenseStatus;
    }

    /**
     * 批量发药
     */
    @Data
    public static class Dispense implements Serializable {

        /** 主键ID集合 */
        @NotEmpty(message = "请选择要发药的带药单")
        private List<Long> ids;

        /** 备注 */
        private String remark;
    }

    /**
     * 删除（仅待发药）
     */
    @Data
    public static class Delete implements Serializable {

        /** 带药单ID */
        @NotNull(message = "带药单ID不能为空")
        private Long id;
    }
}
