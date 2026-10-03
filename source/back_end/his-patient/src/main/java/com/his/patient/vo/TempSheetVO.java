package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 三测单（体温单）VO —— 前端据此**自动画曲线**。
 *
 * <p>后端只负责"按时间升序把点给全 + 给出纵轴范围"，画法在前端。
 * 这样做的原因：三测单的版式各家医院不同（42 格/7 天/单日），把版式写进后端等于把
 * 一个纯展示问题变成接口契约问题；而<b>点必须后端排序</b>——按时间升序是三测单的语义，
 * 不能指望每个调用方都记得排序。
 *
 * <p>{@code points} 只含"该文书类型下有时间点的记录"，且**一条记录一个点**：
 * 三测单按时点唯一（唯一索引保证），所以点不会重。
 */
@Data
public class TempSheetVO implements Serializable {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 床号
     */
    private String bedNo;

    /**
     * 起始日期（含）
     */
    private String beginDate;

    /**
     * 结束日期（含）
     */
    private String endDate;

    /**
     * 数据点个数
     */
    private Integer pointCount;

    /**
     * 体温最高值（纵轴上限用；无体温点时为 null）
     */
    private BigDecimal maxTemperature;

    /**
     * 体温最低值（纵轴下限用；无体温点时为 null）
     */
    private BigDecimal minTemperature;

    /**
     * 脉搏最高值（曲线副轴用；无脉搏点时为 null）
     */
    private Integer maxPulse;

    /**
     * 数据点（按测量时间升序）
     */
    private List<TempPointVO> points;

    /**
     * 三测单上的一个时点
     */
    @Data
    public static class TempPointVO implements Serializable {

        /** 文书ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long recordId;

        /** 测量时间（完整时刻，前端 tooltip 用） */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime measureTime;

        /** 测量日期 yyyy-MM-dd */
        private String measureDate;

        /** 时点 HH:mm */
        private String measureClock;

        /** 班次文案 */
        private String shiftText;

        /** 体温（℃） */
        private BigDecimal temperature;

        /** 脉搏（次/分） */
        private Integer pulse;

        /** 呼吸（次/分） */
        private Integer respiration;

        /** 收缩压（mmHg） */
        private Integer systolicPressure;

        /** 舒张压（mmHg） */
        private Integer diastolicPressure;

        /** 血压文案 "120/80" */
        private String bloodPressureText;

        /** 大便次数 */
        private Integer stoolCount;

        /** 尿量（ml） */
        private Integer urineVolume;

        /** 记录护士 */
        private String nurseName;
    }
}
