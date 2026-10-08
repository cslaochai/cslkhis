package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ICU 专科监护出参。
 */
public class IcuVO {

    /**
     * 入出科记录
     */
    @Data
    public static class StayVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private String stayNo;

        /**
         * 入院ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 患者编号
         */
        private String patientNo;

        /**
         * 患者姓名
         */
        private String patientName;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long fromDeptId;

        private String fromDeptName;

        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;

        /**
         * 病区名称
         */
        private String wardName;

        /**
         * 床位ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long bedId;

        /**
         * 床位号
         */
        private String bedNo;

        private Integer careLevel;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime inTime;

        private String inDiag;

        private Integer inGcs;

        private String inBy;

        private Integer status;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime outTime;

        private Integer outDest;

        private String outReason;

        private Integer outGcs;

        private String outBy;

        private Integer monitorCount;

        /**
         * 备注
         */
        private String remark;

        /**
         * 最近一次监护记录时刻（派生，用于「漏记」提醒）
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastMonitorTime;

        /**
         * 滞留小时数（在科=到现在，已出科=出科-入科）
         */
        private Integer stayHours;
    }

    /**
     * 监护记录
     */
    @Data
    public static class MonitorVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long stayId;

        private String stayNo;

        /**
         * 患者姓名
         */
        private String patientName;

        /**
         * 床位号
         */
        private String bedNo;

        /**
         * 所属入科记录状态（1在科/2已出科）：出科即封账，前端据此隐藏「修改」
         */
        private Integer stayStatus;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime recordTime;

        private BigDecimal temperature;

        private Integer pulse;

        private Integer respiratory;

        private Integer sbp;

        private Integer dbp;

        private Integer spo2;

        private Integer gcsEye;

        private Integer gcsVerbal;

        private Integer gcsMotor;

        private Integer gcsTotal;

        private String pupil;

        private BigDecimal cvp;

        private Integer ventMode;

        private Integer fio2;

        private BigDecimal peep;

        private BigDecimal intakeMl;

        private BigDecimal outputMl;

        private BigDecimal fluidBalance;

        private Integer urineMl;

        private Integer hasAirway;

        private Integer hasCvc;

        private Integer hasArterial;

        private Integer hasCatheter;

        private Integer hasDrain;

        private String conditionDesc;

        private String handling;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long recorderId;

        private String recorderName;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate recordDate;
    }

    /**
     * ICU 床位一览（含在科患者）
     */
    @Data
    public static class BedVO {
        /**
         * 床位ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long bedId;

        /**
         * 床位号
         */
        private String bedNo;

        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;

        /**
         * 病区名称
         */
        private String wardName;

        /**
         * 床位字典状态：0-停用 1-空闲 2-占用（ICU 不反向改写此列，仅展示）
         */
        private Integer bedStatus;

        /**
         * 床位类型（本域只认 'ICU' 床）
         */
        private String bedType;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long stayId;

        private String stayNo;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 患者编号
         */
        private String patientNo;

        /**
         * 患者姓名
         */
        private String patientName;

        private Integer careLevel;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime inTime;

        private Integer inGcs;

        private Integer monitorCount;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastMonitorTime;

        private Integer lastSbp;

        private Integer lastDbp;

        private Integer lastPulse;

        private Integer lastSpo2;

        private BigDecimal lastTemperature;

        private Integer lastGcsTotal;
    }

    /**
     * 可入科候选（在院、无在科记录）
     */
    @Data
    public static class AdmissionVO {
        /**
         * 入院ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        private String admissionNo;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 患者编号
         */
        private String patientNo;

        /**
         * 患者姓名
         */
        private String patientName;

        /**
         * 性别（1-男 2-女 9-未知）
         */
        private Integer gender;

        /**
         * 年龄
         */
        private Integer age;

        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        /**
         * 科室名称
         */
        private String deptName;

        /**
         * 床位号
         */
        private String bedNo;

        /**
         * 诊断
         */
        private String diagnosis;

        /**
         * 入院状态：1-在院（候选查询已过滤，快照校验用）
         */
        private Integer admitStatus;

        /**
         * 入院时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime admitTime;
    }

    /**
     * 分布计数（监护等级、呼吸支持方式等）
     */
    @Data
    public static class TypeCount {
        private Integer type;

        private Integer count;
    }

    /**
     * 科室工作量与质量安全指标
     */
    @Data
    public static class StatsVO {
        /**
         * 开始日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /**
         * 结束日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        /**
         * 当前在科人数
         */
        private Integer inCount;

        /**
         * ICU 床位数（可用口径，不含停用）
         */
        private Integer bedTotal;

        /**
         * 床位使用率 % = 在科/开放床位
         */
        private BigDecimal bedUseRate;

        private Integer inCountRange;

        private Integer outCountRange;

        private Integer monitorTotalRange;

        /**
         * 人均监护记录条数（分母=区间入科+出科记录数）
         */
        private BigDecimal monitorsPerStay;

        /**
         * 死亡人数（出科去向=5）
         */
        private Integer deathCount;

        /**
         * 平均滞留小时（区间内出科者）
         */
        private BigDecimal avgStayHours;

        private List<TypeCount> careLevels;

        /**
         * 最近一条监护记录的呼吸支持方式分布
         */
        private List<TypeCount> ventModes;

        /**
         * 五类导管现带管人数：人工气道/中心静脉/动脉/导尿管/引流管
         */
        private Integer airwayCount;

        private Integer cvcCount;

        private Integer arterialCount;

        private Integer catheterCount;

        private Integer drainCount;

        /**
         * 超过 N 小时无监护记录的在科人数（护理质量预警）
         */
        private Integer monitorLagCount;
    }
}
