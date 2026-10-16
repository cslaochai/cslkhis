package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 绩效成本 VO。
 */
public class PerfVO {

    @Data
    public static class CostRow {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 科室ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /** 科室名称 */
        private String deptName;
        /** 核算月份 */
        private String costMonth;
        private BigDecimal laborCost;
        private BigDecimal drugCost;
        private BigDecimal materialCost;
        private BigDecimal depreciation;
        private BigDecimal otherCost;
        /** 成本合计 */
        private BigDecimal totalCost;
        /** 备注 */
        private String remark;
        /** 创建人 */
        private String createBy;
        /** 创建时间 */
        private LocalDateTime createTime;
    }

    @Data
    public static class PerfRow {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 科室ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /** 科室名称 */
        private String deptName;
        /** 核算月份 */
        private String costMonth;
        /** 科室收入 */
        private BigDecimal revenue;
        /** 药品收入 */
        private BigDecimal drugRevenue;
        /** 药占比 */
        private BigDecimal drugRatio;
        /** 成本合计 */
        private BigDecimal totalCost;
        /** 结余 = 收入 - 成本 */
        private BigDecimal surplus;
        /** 提成系数 */
        private BigDecimal bonusRate;
        /** 绩效金额 = max */
        private BigDecimal perfAmount;
        /** 状态（1-草稿 2-已核算 3-已发布） */
        private Integer perfStatus;
        /** 备注 */
        private String remark;
        /** 更新时间 */
        private LocalDateTime updateTime;
    }

    /**
     * 执行核算的输入回显（收入聚合结果）
     */
    @Data
    public static class RevenueInfo {
        /** 科室ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /** 科室名称 */
        private String deptName;
        /** 核算月份 */
        private String costMonth;
        /** 科室收入 */
        private BigDecimal revenue;
        /** 药品收入 */
        private BigDecimal drugRevenue;
        /** 成本是否已录入 */
        private Boolean costExists;
        /** 成本合计 */
        private BigDecimal totalCost;
        /** 结余（成本未录时为收入全额） */
        private BigDecimal surplus;
        /** 成本记录 ID（未录时为空） */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long costId;
    }
}
