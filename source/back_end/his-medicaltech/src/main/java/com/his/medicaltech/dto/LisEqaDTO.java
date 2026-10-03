package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 室间质评入参
 *
 * <p>通例：<b>状态、成绩、判定一律不进 DTO</b>。DTO 里只装"人填的东西"
 * ——组织方叫什么、样品第几号、本室测出来多少 —— 剩下全是服务端算。
 */
public class LisEqaDTO {

    // 批次

    @Data
    public static class PlanUpsert {
        private Long id;

        @NotNull(message = "质评年度不能为空")
        private Integer planYear;

        @NotNull(message = "批次序号不能为空")
        private Integer batchNo;

        @NotBlank(message = "组织方不能为空")
        private String orgName;

        private String planName;

        /** 盲样接收日期 */
        private LocalDate receiveDate;

        /** 盲样接收人 */
        private String receiveBy;

        /** 结果上报截止日（用于逾期标记，不做拦截：过了截止日数据也还得报出去） */
        private LocalDate reportDeadline;

        /** 备注 */
        private String remark;
    }

    @Data
    public static class PlanQuery {
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 20;

        private Integer planYear;

        private Integer batchNo;

        private String orgName;

        private Integer status;
    }

    @Data
    public static class PlanArchive {
        /** 质评批次ID */
        @NotNull(message = "质评批次ID不能为空")
        private Long planId;
    }

    // 盲样台账

    @Data
    public static class SampleUpsert {
        private Long id;

        /** 质评批次ID */
        @NotNull(message = "质评批次ID不能为空")
        private Long planId;

        /** 盲样编号 */
        @NotBlank(message = "盲样编号不能为空")
        private String sampleNo;

        /** 第几个样品 */
        @NotNull(message = "样品序号不能为空")
        private Integer sampleSeq;

        /** 检验项目ID */
        private Long itemId;

        /**
         * 检验项目编码（必填）——不是刁难：室间质评盲样的唯一键是
         * （批次 + 样品序号 + 项目编码 + 仪器），编码留 NULL 会让唯一键整个失效，
         * 同一盲样能被重复登记两行，PT 得分的分母就废了。
         */
        @NotBlank(message = "检验项目编码不能为空")
        private String itemCode;

        /** 检验项目名称 */
        @NotBlank(message = "检验项目名称不能为空")
        private String itemName;

        /** 检测仪器（室间差按此分组；不填表示本批次只用一套仪器） */
        private String instrumentName;

        /** 检测方法学 */
        private String methodName;

        /** 盲样接收日期 */
        private LocalDate receiveDate;

        /** 盲样接收人 */
        private String receiveBy;

        /** 备注 */
        private String remark;
    }

    @Data
    public static class ItemRow {
        /** 检验项目编码 */
        @NotBlank(message = "检验项目编码不能为空")
        private String itemCode;

        /** 检验项目名称 */
        @NotBlank(message = "检验项目名称不能为空")
        private String itemName;
    }

    /**
     * 批量生成盲样台账骨架：样品序号 × 项目 × 仪器笛卡尔积。
     * 已经存在的组合跳过（不覆盖 —— 已录过检测值的行被覆盖等于抹掉检测记录）。
     */
    @Data
    public static class SampleGenerate {
        /** 质评批次ID */
        @NotNull(message = "质评批次ID不能为空")
        private Long planId;

        /** 盲样编号前缀，实际编号 = 前缀 + "-" + 样品序号 */
        @NotBlank(message = "盲样编号前缀不能为空")
        private String sampleNoPrefix;

        @NotEmpty(message = "样品序号列表不能为空")
        private List<Integer> sampleSeqs;

        /** 明细项集合 */
        @NotEmpty(message = "检验项目列表不能为空")
        private List<ItemRow> items;

        /** 检测仪器列表（留空给一套默认，填多台仪器才有"室间差"可比） */
        private List<String> instruments;

        /** 检测方法学 */
        private String methodName;

        /** 盲样接收日期 */
        private LocalDate receiveDate;

        /** 盲样接收人 */
        private String receiveBy;
    }

    @Data
    public static class SampleQuery {
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 20;

        /** 质评批次ID */
        private Long planId;

        /** 质评批次号（冗余） */
        private String planNo;

        /** 盲样编号 */
        private String sampleNo;

        /** 第几个样品 */
        private Integer sampleSeq;

        /** 检验项目名称 */
        private String itemName;

        /** 检测仪器 */
        private String instrumentName;

        /** 流转状态（0-待检测 1-已检测 2-已上报 3-已回报） */
        private Integer status;

        /** 判定结果（0-未判定 1-满意 2-尚可 3-不合格） */
        private Integer resultStatus;

        /** 整改状态（0-无需整改 1-待整改 2-已整改） */
        private Integer handleStatus;
    }

    // 检测 / 上报 / 回报

    @Data
    public static class TestInput {
        @NotNull(message = "盲样台账ID不能为空")
        private Long sampleId;

        /** 本室测定值 */
        @NotNull(message = "本室测定值不能为空")
        private BigDecimal testValue;

        /** 备注 */
        private String remark;
    }

    @Data
    public static class ReportInput {
        /** 质评批次ID */
        @NotNull(message = "质评批次ID不能为空")
        private Long planId;

        @NotEmpty(message = "待上报的盲样不能为空")
        private List<Long> sampleIds;
    }

    @Data
    public static class ScoreRow {
        @NotNull(message = "盲样台账ID不能为空")
        private Long sampleId;

        /** 回报靶值 / 组均值 */
        private BigDecimal targetValue;

        /** 组标准差（SDI 口径要用） */
        private BigDecimal groupSd;

        /** 允许总误差 TEa（%） */
        private BigDecimal tea;

        /** 可接受范围下限 */
        private BigDecimal targetMin;

        /** 可接受范围上限 */
        private BigDecimal targetMax;
    }

    @Data
    public static class ScoreReturn {
        /** 质评批次ID */
        @NotNull(message = "质评批次ID不能为空")
        private Long planId;

        private LocalDate returnDate;

        @NotEmpty(message = "回报明细不能为空")
        private List<ScoreRow> rows;
    }

    @Data
    public static class CompareQuery {
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 20;

        /** 质评批次ID */
        private Long planId;

        /** 检验项目名称 */
        private String itemName;

        /** 1-可接受 2-超差 */
        private Integer status;
    }

    // 不合格整改

    @Data
    public static class Rectify {
        @NotNull(message = "盲样台账ID不能为空")
        private Long sampleId;

        /** 不合格原因分析 */
        @NotBlank(message = "不合格原因不能为空")
        private String handleCause;

        /** 纠正措施 */
        @NotBlank(message = "纠正措施不能为空")
        private String handleMeasure;
    }

    @Data
    public static class RectifyReview {
        @NotNull(message = "盲样台账ID不能为空")
        private Long sampleId;
    }
}
