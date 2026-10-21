package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DIP 模拟 VO。
 */
public class DipSimVO {

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
        /** 分组时使用的手术操作编码 */
        private String operCode;
        /** 手术操作名称 */
        private String operName;
        private Boolean surgery;
        /** 住院天数 */
        private Integer inpatientDays;
        /** 病种编码 */
        private String dipCode;
        /** 病种名称 */
        private String dipName;
        /** 入组病种类型（1-核心病种 2-综合病种） */
        private Integer diseaseType;
        /** 病种库版本 */
        private String version;
        /** 基准分值 */
        private BigDecimal baseScore;
        /** 病例组合系数 */
        private BigDecimal coeff;
        /** 结算分值 = 基准分值 × 系数 */
        private BigDecimal score;
        /** 点值（元/分值，统筹区公布） */
        private BigDecimal pointValue;
        /** 预测结算金额 = 分值 × 点值 */
        private BigDecimal settleAmount;
        /** 实际住院费用 */
        private BigDecimal actualAmount;
        /** 盈亏：正=结余，负=超支；未入组为空 */
        private BigDecimal profitAmount;
        /** 结果（1-已入组 2-未入组） */
        private Integer simStatus;
        /** 命中路径说明 */
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
        /** 手术操作编码 */
        private String operCode;
        /** 手术操作名称 */
        private String operName;
        /** 入组病种类型 */
        private Integer diseaseType;
        /** 病种编码 */
        private String dipCode;
        /** 病种名称 */
        private String dipName;
        /** 病种库版本 */
        private String version;
        private BigDecimal baseScore;
        private BigDecimal coeff;
        private BigDecimal score;
        private BigDecimal pointValue;
        /** 预测结算金额（元） */
        private BigDecimal settleAmount;
        /** 实际住院费用 */
        private BigDecimal actualAmount;
        /** 盈亏 = 结算金额 - 实际费用 */
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
        private String simDipCode;
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

    /**
     * 病种目录一行
     */
    @Data
    public static class DiseaseRow {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 病种编码 */
        private String diseaseCode;
        /** 病种名称 */
        private String diseaseName;
        /** 病种类型（1-核心病种 2-综合病种） */
        private Integer diseaseType;
        /** 主诊断编码 */
        private String mainDiagCode;
        /** 主诊断名称 */
        private String mainDiagName;
        /** 手术操作编码 */
        private String operCode;
        /** 手术操作名称 */
        private String operName;
        /** 主诊断匹配方式（1-整码 2-前缀） */
        private Integer diagMatch;
        /** 基准分值 */
        private BigDecimal baseScore;
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
