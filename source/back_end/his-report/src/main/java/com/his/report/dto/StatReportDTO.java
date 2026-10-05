package com.his.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 病案统计上报 DTO。
 */
public class StatReportDTO {

    /**
     * 生成上报报文（打印预留：聚合落库留痕，不对外发送）
     */
    @Data
    public static class Generate {
        /**
         * 报表类型（1-卫统年报 2-出院患者统计月报 3-手术工作量专项报表）
         */
        @NotNull(message = "请选择上报类型")
        private Integer reportType;
        /**
         * 期间类型（1-月报 2-年报）
         */
        @NotNull(message = "请选择期间类型")
        private Integer periodType;
        /**
         * yyyy-MM（月报）或 yyyy（年报）
         */
        @NotBlank(message = "上报期间不能为空")
        private String periodValue;
        /**
         * 空=全院口径
         */
        private Long deptId;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 台账分页查询
     */
    @Data
    public static class QueryPage {
        /**
         * 页码
         */
        private Integer pageNum = 1;
        /**
         * 每页条数
         */
        private Integer pageSize = 10;
        /**
         * 上报单号/标题模糊
         */
        private String keyword;
        /**
         * 报表类型（1-卫统年报 2-出院患者统计月报 3-手术工作量专项报表）
         */
        private Integer reportType;
        /**
         * 状态（0-草稿 1-已报出 2-已作废）
         */
        private Integer status;
        /**
         * 期间值
         */
        private String periodValue;
        /**
         * 开始日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startDate;
        /**
         * 结束日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endDate;
    }

    /**
     * 报出（真实对接时换成 http 上报，此处置状态留痕）
     */
    @Data
    public static class Submit {
        /**
         * 主键ID
         */
        @NotNull(message = "缺少上报台账ID")
        private Long id;
    }

    /**
     * 作废（释放同期槽位，payload 留痕不删）
     */
    @Data
    public static class VoidReq {
        /**
         * 主键ID
         */
        @NotNull(message = "缺少上报台账ID")
        private Long id;
        /**
         * 原因
         */
        @NotBlank(message = "作废原因不能为空")
        private String reason;
    }
}
