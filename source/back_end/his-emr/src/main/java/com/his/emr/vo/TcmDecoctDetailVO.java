package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 代煎单详情（回执与调剂核对用：逐味列出每剂克数、总克数、煎法脚注）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TcmDecoctDetailVO extends TcmDecoctVO {

    /**
     * 逐味明细（来自处方明细，按录入顺序）
     */
    private List<HerbLine> herbs;

    @Data
    public static class HerbLine {

        /** 主键ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private String drugCode;

        /** 药品名称 */
        private String drugName;

        private String specification;

        /**
         * 每剂克数（single_dosage 原文，如「15」「15g」）
         */
        private String perDoseText;

        /**
         * 本味实发总克数（= quantity，饮片方按克存）
         */
        private BigDecimal grams;

        /**
         * 煎法脚注（route 原文，如「先煎」「后下」「包煎」）
         */
        private String method;
    }
}
