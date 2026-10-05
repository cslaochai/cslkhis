package com.his.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医废登记 DTO 集合。
 *
 * <p>collectTime 按 yyyy-MM-dd HH:mm:ss 宽进（AGENTS.md 日期格式铁律）。
 */
public class WasteDTO {

    /**
     * 医废登记
     */
    @Data
    public static class Create implements Serializable {

        /**
         * 医废类别（1-感染性 2-损伤性 3-病理性 4-药物性 5-化学性）
         */
        @NotNull(message = "医废类别不能为空")
        private Integer wasteType;

        /**
         * 重量（kg）
         */
        private BigDecimal weightKg;

        /**
         * 产生科室ID
         */
        private Long deptId;

        /**
         * 产生科室名称
         */
        private String deptName;

        /**
         * 收集时间
         */
        @NotNull(message = "收集时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime collectTime;

        /**
         * 收集人（不填取当前登录人）
         */
        private String collectorName;
    }

    /**
     * 交接（1→2）
     */
    @Data
    public static class Handover implements Serializable {

        @NotNull(message = "登记ID不能为空")
        private Long id;

        /**
         * 交接人
         */
        @NotBlank(message = "交接人不能为空")
        private String handoverName;
    }

    /**
     * 处置确认（2→3）
     */
    @Data
    public static class Dispose implements Serializable {

        @NotNull(message = "登记ID不能为空")
        private Long id;

        /**
         * 处置公司
         */
        @NotBlank(message = "处置公司不能为空")
        private String disposalCompany;
    }

    /**
     * 分页查询
     */
    @Data
    public static class QueryPage implements Serializable {

        /**
         * 关键词：交接单号/科室模糊
         */
        private String keyword;

        /**
         * 医废类别（1-感染性 2-损伤性 3-病理性 4-药物性 5-化学性）
         */
        private Integer wasteType;

        private Integer status;

        /**
         * 收集日期起（含当天 00:00:00）
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate collectDateBegin;

        /**
         * 收集日期止（含当天 23:59:59，日期边界铁律）
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate collectDateEnd;

        /**
         * 页码
         */
        private Integer pageNum = 1;

        /**
         * 每页条数
         */
        private Integer pageSize = 10;
    }
}
