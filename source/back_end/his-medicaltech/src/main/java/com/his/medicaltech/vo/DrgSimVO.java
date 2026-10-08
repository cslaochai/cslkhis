package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DRG 模拟 VO。
 */
public class DrgSimVO {

    /**
     * 单条/批量模拟结果
     */
    @Data
    public static class SimResult {
        /** 病案首页ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long summaryId;
        /** 患者姓名 */
        private String patientName;
        /** 分组时使用的主诊断编码 */
        private String mainDiagCode;
        /** 主诊断名称 */
        private String mainDiagName;
        private Boolean surgery;
        /** 住院天数 */
        private Integer inpatientDays;
        /** 入组编码 */
        private String drgCode;
        /** 组名称 */
        private String drgName;
        /** MDC 大类 */
        private String mdcCode;
        private BigDecimal weight;
        /** 病组支付标准（元） */
        private BigDecimal payStandard;
        /** 实际住院费用 */
        private BigDecimal actualAmount;
        /** 盈亏：正=结余，负=超支；未入组为空 */
        private BigDecimal profitAmount;
        /** 结果（1-已入组 2-未入组） */
        private Integer simStatus;
        /** 命中规则说明 */
        private String ruleNote;
    }

    /**
     * 已模拟结果分页行
     */
    @Data
    public static class ResultRow {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 病案首页ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long summaryId;
        /** 患者姓名 */
        private String patientName;
        /** 分组时使用的主诊断编码 */
        private String mainDiagCode;
        /** 主诊断名称 */
        private String mainDiagName;
        /** 是否手术 */
        private Integer isSurgery;
        /** 住院天数 */
        private Integer inpatientDays;
        /** 入组编码 */
        private String drgCode;
        /** 组名称 */
        private String drgName;
        private BigDecimal weight;
        /** 病组支付标准（元） */
        private BigDecimal payStandard;
        /** 实际住院费用 */
        private BigDecimal actualAmount;
        /** 盈亏 = 支付标准 - 实际费用 */
        private BigDecimal profitAmount;
        /** 结果（1-已入组 2-未入组） */
        private Integer simStatus;
        /** 命中规则说明 */
        private String ruleNote;
        /** 更新时间 */
        private LocalDateTime updateTime;
    }

    /**
     * 可模拟首页行
     */
    @Data
    public static class SummaryRow {
        /** 病案首页ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long summaryId;
        /** 患者姓名 */
        private String patientName;
        /** 科室名称 */
        private String deptName;
        private LocalDateTime dischargeTime;
        /** 分组时使用的主诊断编码 */
        private String mainDiagCode;
        /** 主诊断名称 */
        private String mainDiagName;
        /** 是否手术 */
        private Integer isSurgery;
        private String simDrgCode;
        private BigDecimal simProfit;
    }

    /**
     * 汇总卡
     */
    @Data
    public static class SimStat {
        /** 总条数 */
        private Long total;
        private Long grouped;
        private Long ungrouped;
        private BigDecimal totalProfit;
        private BigDecimal totalOverrun;
    }

    @Data
    public static class GroupRow {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 入组编码 */
        private String drgCode;
        /** 组名称 */
        private String drgName;
        /** MDC 大类 */
        private String mdcCode;
        private String adrgCode;
        private BigDecimal weight;
        /** 病组支付标准（元） */
        private BigDecimal payStandard;
        private String source;
        private String version;
        private Integer status;
    }

    /**
     * 列表包装
     */
    @Data
    public static class SummaryListVO {
        private List<SummaryRow> rows;
        private SimStat stat;
    }
}
