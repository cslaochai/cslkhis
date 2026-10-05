package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * LIS 室内质控出参
 */
public class LisQcVO {

    @Data
    public static class PlanVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private String planNo;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long itemId;

        private String itemCode;

        /**
         * 项目名称
         */
        private String itemName;

        private String instrumentNo;

        private String instrumentName;

        private Integer qcLevel;

        private String qcLevelText;

        private String controlName;

        private String controlLotNo;

        private String manufacturer;

        private BigDecimal meanValue;

        private BigDecimal sdValue;

        private BigDecimal cvLimit;

        /**
         * 实际 CV = SD / 靶值 × 100%（服务端算，超过 cvLimit 即为靶值漂移）
         */
        private BigDecimal cvActual;

        private LocalDate expireDate;

        private Integer status;

        /**
         * 状态文本
         */
        private String statusText;

        /**
         * 该计划累计质控点数
         */
        private Integer recordCount;
    }

    @Data
    public static class RecordVO {
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

        private String instrumentName;

        private Integer qcLevel;

        private String qcLevelText;

        private LocalDate qcDate;

        private LocalDateTime qcTime;

        private BigDecimal resultValue;

        // ⚠ 字段名 zScore（z+大写）会被 Lombok getZScore + Beans 规则推导成全小写 "zscore"，
        // 必须 @JsonProperty 钉死键名，否则前端读 data.zScore 恒 undefined
        @com.fasterxml.jackson.annotation.JsonProperty("zScore")
        private BigDecimal zScore;

        /**
         * 1-在控 2-警告 3-失控；0-未判定（靶值/SD 缺失）
         */
        private Integer status;

        /**
         * 状态文本
         */
        private String statusText;

        private String violatedRules;

        private String operator;

        private Integer handleStatus;

        private String handleStatusText;

        private String handleCause;

        private String handleMeasure;

        private String handleBy;

        private LocalDateTime handleTime;

        private String reviewBy;

        private LocalDateTime reviewTime;
    }

    @Data
    public static class StatsVO {
        private long planCount;
        private long todayCount;
        private long inControl;
        private long warning;
        private long outOfControl;
        private long pendingHandle;
        /**
         * 在控率（%），分母为今日质控点数；今日无数据时为 0
         */
        private BigDecimal inControlRate;
    }
}
