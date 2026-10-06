package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 病案统计上报 VO。
 */
public class StatReportVO {

    /** 台账列表行（不含 payload 大字段） */
    @Data
    public static class Row {
        /** 主键ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 上报单号 */
        private String reportNo;
        /** 报表类型（1-卫统年报 2-出院患者统计月报 3-手术工作量专项报表） */
        private Integer reportType;
        /** 期间类型（1-月报 2-年报） */
        private Integer periodType;
        /** 期间值 */
        private String periodValue;
        /** 科室ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /** 科室名称 */
        private String deptName;
        /** 报表标题 */
        private String title;
        /** 摘要-出院患者例数 */
        private Integer dischargeCount;
        /** 摘要-死亡例数 */
        private Integer deathCount;
        /** 摘要-手术台次 */
        private Integer operationCount;
        /** 摘要-三级及以上手术台次 */
        private Integer level3upCount;
        /** 摘要-平均住院日 */
        private BigDecimal avgLosDays;
        /** 摘要-结算总金额 */
        private BigDecimal totalAmount;
        /** 状态（0-草稿 1-已报出 2-已作废） */
        private Integer status;
        /** 报文生成时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime generateTime;
        /** 报出时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime submitTime;
        /** 生成人 */
        private String operatorName;
        /** 报出人 */
        private String submitByName;
        /** 作废原因 */
        private String voidReason;
        /** 备注 */
        private String remark;
    }

    /** 明细（含报文原文，前端预览/打印用） */
    @Data
    public static class Detail extends Row {
        /** 上报报文 */
        private String payload;
        /** 作废时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime voidTime;
    }
}
