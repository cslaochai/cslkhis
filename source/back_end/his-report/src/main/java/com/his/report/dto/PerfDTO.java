package com.his.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 绩效成本 DTO。
 */
public class PerfDTO {

    /** 科室月度成本录入 */
    @Data
    public static class CostSave {
        /** 科室ID */
        @NotNull(message = "科室不能为空")
        private Long deptId;
        /** 未填时后端快照科室名 */
        private String deptName;
        /** 核算月份 */
        @NotBlank(message = "核算月份不能为空")
        private String costMonth;
        /** 人力成本（元） */
        private BigDecimal laborCost;
        /** 药品成本（元） */
        private BigDecimal drugCost;
        /** 耗材成本（元） */
        private BigDecimal materialCost;
        /** 设备折旧（元） */
        private BigDecimal depreciation;
        /** 其他成本（元） */
        private BigDecimal otherCost;
        /** 备注 */
        private String remark;
    }

    /** 成本分页 */
    @Data
    public static class CostQuery {
        /** 科室ID */
        private Long deptId;
        /** 核算月份 */
        private String costMonth;
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 10;
    }

    /** 绩效核算 */
    @Data
    public static class PerfCalc {
        /** 科室ID */
        @NotNull(message = "科室不能为空")
        private Long deptId;
        /** 核算月份 */
        @NotBlank(message = "核算月份不能为空")
        private String costMonth;
        /** 提成系数，默认 0.06 */
        private BigDecimal bonusRate;
    }

    /** 绩效结果分页 */
    @Data
    public static class PerfQuery {
        /** 科室ID */
        private Long deptId;
        /** 核算月份 */
        private String costMonth;
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 10;
    }
}
