package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 室间质评出参
 *
 * <p>凡是带 Text 后缀的字段都由后端从字典数据回填，前端不自己拼中文；
 * sdi / biasRate / resultStatus 这类<b>结论字段</b>前端只读不改。
 */
public class LisEqaVO {

    @Data
    public static class PlanVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private String planNo;

        private Integer planYear;

        private Integer batchNo;

        private String orgName;

        private String planName;

        private Integer itemCount;

        private Integer sampleCount;

        private LocalDate receiveDate;

        private String receiveBy;

        private LocalDate reportDeadline;

        private LocalDate returnDate;

        private Integer status;

        /**
         * 状态文本
         */
        private String statusText;

        /**
         * PT 得分（%），服务端算
         */
        private BigDecimal ptScore;

        /**
         * 1-合格 0-不合格（PT ≥ 80%）
         */
        private Integer passFlag;

        private String passFlagText;

        private Integer failCount;

        /**
         * 未回报成绩的盲样项数（>0 说明 PT 得分还是暂定值）
         */
        private Integer pendingCount;

        private String archiveBy;

        private LocalDateTime archiveTime;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    public static class SampleVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long planId;

        private String planNo;

        private String sampleNo;

        private Integer sampleSeq;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long itemId;

        private String itemCode;

        /**
         * 项目名称
         */
        private String itemName;

        private String instrumentName;

        private String methodName;

        private LocalDate receiveDate;

        private String receiveBy;

        // 本室检测
        private BigDecimal testValue;

        private String testBy;

        private LocalDateTime testTime;

        private Integer overdueFlag;

        // 组织方回报
        private BigDecimal targetValue;

        private BigDecimal groupSd;

        private BigDecimal tea;

        private BigDecimal targetMin;

        private BigDecimal targetMax;

        // 服务端判定
        private BigDecimal sdi;

        private BigDecimal biasRate;

        private Integer judgeMode;

        private String judgeModeText;

        private Integer resultStatus;

        private String resultStatusText;

        private Integer status;

        /**
         * 状态文本
         */
        private String statusText;

        private Integer handleStatus;

        private String handleStatusText;

        private String handleCause;

        private String handleMeasure;

        private String handleBy;

        private LocalDateTime handleTime;

        private String reviewBy;

        private LocalDateTime reviewTime;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    public static class CompareVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long planId;

        private String planNo;

        private String itemCode;

        /**
         * 项目名称
         */
        private String itemName;

        private Integer sampleSeq;

        private String sampleNo;

        private String instrumentA;

        private String methodA;

        private BigDecimal valueA;

        private String instrumentB;

        private String methodB;

        private BigDecimal valueB;

        private BigDecimal diffValue;

        private BigDecimal diffRate;

        private BigDecimal allowRate;

        private Integer allowSource;

        private String allowSourceText;

        private Integer status;

        /**
         * 状态文本
         */
        private String statusText;
    }

    /**
     * 成绩回报后回给前端的汇总（值即结论，页面不用再算一遍）
     */
    @Data
    public static class JudgeVO {
        private String planNo;

        private int returnedCount;

        private int passCount;

        private int failCount;

        private BigDecimal ptScore;

        private Integer passFlag;

        private String passFlagText;

        private int compareCount;

        private int compareFailedCount;
    }

    @Data
    public static class StatsVO {
        /**
         * 在评批次（状态 1~4，未归档）
         */
        private long activePlanCount;

        /**
         * 已上报、等组织方回成绩的批次
         */
        private long pendingReturnCount;

        /**
         * 待整改的不合格项
         */
        private long pendingRectifyCount;

        /**
         * 7 天内截止（含已逾期）尚未上报的批次
         */
        private long dueSoonCount;

        /**
         * 本年度不合格项数
         */
        private long yearFailCount;

        /**
         * 本年度各批次 PT 平均分
         */
        private BigDecimal yearAvgScore;

        /**
         * 本年度已出成绩的批次数（yearAvgScore 的分母）
         */
        private long yearScoredCount;

        private Integer thisYear;
    }
}
