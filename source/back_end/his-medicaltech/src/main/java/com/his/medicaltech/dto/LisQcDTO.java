package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * LIS 室内质控入参
 */
public class LisQcDTO {

    /**
     * 质控计划新增 / 修改
     */
    @Data
    public static class PlanUpsert {
        private Long id;

        /**
         * 检验项目ID
         */
        private Long itemId;

        /**
         * 检验项目编码
         */
        private String itemCode;

        /**
         * 检验项目名称
         */
        @NotBlank(message = "检验项目名称不能为空")
        private String itemName;

        /**
         * 仪器编号
         */
        private String instrumentNo;

        /**
         * 仪器名称
         */
        private String instrumentName;

        /**
         * 质控水平（1-低值 2-中值 3-高值）
         */
        private Integer qcLevel;

        /**
         * 质控品名称
         */
        private String controlName;

        /**
         * 质控品批号
         */
        private String controlLotNo;

        /**
         * 生产厂家
         */
        private String manufacturer;

        /**
         * 靶值（均值）
         */
        @NotNull(message = "靶值不能为空")
        private BigDecimal meanValue;

        /**
         * 标准差 SD
         */
        @NotNull(message = "标准差 SD 不能为空")
        private BigDecimal sdValue;

        /**
         * 允许 CV 上限（%）
         */
        private BigDecimal cvLimit;

        /**
         * 质控品效期
         */
        private LocalDate expireDate;

        /**
         * 状态（1-启用 0-停用）
         */
        private Integer status;
    }

    /**
     * 质控计划启用/停用
     */
    @Data
    public static class PlanToggle {
        @NotNull(message = "质控计划ID不能为空")
        private Long planId;

        /**
         * 状态（0-停用 1-启用）
         */
        @NotNull(message = "状态不能为空")
        @Min(value = 0, message = "状态取值不合法（0-停用 1-启用）")
        @Max(value = 1, message = "状态取值不合法（0-停用 1-启用）")
        private Integer status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class PlanQuery extends PageParam {
        /**
         * 检验项目名称
         */
        private String itemName;

        /**
         * 仪器名称
         */
        private String instrumentName;

        /**
         * 只看启用
         */
        private Integer status;
    }

    /**
     * 录入质控测定值（质控时间由服务端取当前时间）
     */
    @Data
    public static class ResultInput {
        @NotNull(message = "质控计划ID不能为空")
        private Long planId;

        @NotNull(message = "质控测定值不能为空")
        private BigDecimal resultValue;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class RecordQuery extends PageParam {
        private Long planId;

        /**
         * 检验项目名称
         */
        private String itemName;

        /**
         * 仪器名称
         */
        private String instrumentName;

        private Integer status;

        /**
         * 只看待处理的失控
         */
        private Integer handleStatus;

        /**
         * 开始日期
         */
        private LocalDate startDate;

        /**
         * 结束日期
         */
        private LocalDate endDate;
    }

    /**
     * 失控处理：原因 + 纠正措施
     */
    @Data
    public static class Handle {
        @NotNull(message = "质控记录ID不能为空")
        private Long recordId;

        @NotBlank(message = "失控原因不能为空")
        private String handleCause;

        @NotBlank(message = "纠正措施不能为空")
        private String handleMeasure;
    }

    @Data
    public static class Review {
        @NotNull(message = "质控记录ID不能为空")
        private Long recordId;
    }
}
