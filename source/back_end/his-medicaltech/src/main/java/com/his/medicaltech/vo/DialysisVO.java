package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 血液净化（透析）出参。
 *
 * <p>档案/排班的患者与处方信息一律取库内快照列，不回查患者基本信息，
 * 因此模板改档、患者改名后历史单仍能自证当时口径。
 */
public class DialysisVO {

    /** 透析患者档案 */
    @Data
    public static class ArchiveVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private String dialysisNo;

        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /** 患者编号 */
        private String patientNo;

        /** 患者姓名 */
        private String patientName;

        /** 明文电话，仅编辑回显接口（archiveGetById）返回 */
        private String phone;

        /** 展示用脱敏电话，列表接口返回；明文侧置 null */
        private String phoneMasked;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate firstDialysisDate;

        private String cause;

        private Integer accessType;

        private String accessSite;

        private Integer dialysisFreq;

        private Integer status;

        private String exitReason;

        /** 备注 */
        private String remark;

        /** 累计透析例次（派生） */
        private Integer sessionTotal;

        /** 已完成例次（派生） */
        private Integer sessionDone;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate lastSessionDate;

        /** 当前有效处方ID（无有效处方时为 null，排班会被挡） */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long activePrescriptionId;

        private BigDecimal dryWeight;

        private Integer durationMin;

        private Integer bloodFlow;

        private Integer dialyzer;

        private Integer anticoagulant;

        /** 创建时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    /** 透析处方 */
    @Data
    public static class PrescriptionVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long archiveId;

        /** 患者姓名 */
        private String patientName;

        private BigDecimal dryWeight;

        private Integer durationMin;

        private Integer bloodFlow;

        private Integer dialyzer;

        private Integer anticoagulant;

        private String anticoagDose;

        private BigDecimal targetUltraMl;

        /** 开始日期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /** 结束日期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        private Integer status;

        /** 医生ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long doctorId;

        /** 医生姓名 */
        private String doctorName;

        private String stopReason;

        /** 备注 */
        private String remark;

        /** 创建时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    /** 透析机位 */
    @Data
    public static class MachineVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private String machineNo;

        private String roomName;

        private Integer status;

        /** 备注 */
        private String remark;

        /** 占用例次（台账用，派生） */
        private Integer sessionTotal;
    }

    /** 透析单（排班 + 治疗记录） */
    @Data
    public static class SessionVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private String sessionNo;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dialysisDate;

        private Integer timeSlot;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long machineId;

        private String machineNo;

        private String roomName;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long archiveId;

        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /** 患者编号 */
        private String patientNo;

        /** 患者姓名 */
        private String patientName;

        /** 处方ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long prescriptionId;

        private BigDecimal dryWeight;

        private Integer durationMin;

        private Integer bloodFlow;

        private Integer dialyzer;

        private Integer anticoagulant;

        private Integer status;

        private BigDecimal beforeWeight;

        private String accessCheck;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime onTime;

        private String onBy;

        private BigDecimal afterWeight;

        private Integer actualDurationMin;

        private BigDecimal ultraMl;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime offTime;

        private String offBy;

        private Integer adverseType;

        private String adverseDesc;

        /** 取消原因 */
        private String cancelReason;

        /** 备注 */
        private String remark;

        /** 创建时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    /** 机位时段看板的一格 */
    @Data
    public static class BoardCellVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long machineId;

        private String machineNo;

        private String roomName;

        /** 机位状态（2维修/3停用的机位不允许排班，看板上仍要看得见） */
        private Integer machineStatus;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long sessionId;

        private String sessionNo;

        private Integer sessionStatus;

        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /** 患者编号 */
        private String patientNo;

        /** 患者姓名 */
        private String patientName;

        private BigDecimal beforeWeight;

        private BigDecimal afterWeight;

        private BigDecimal ultraMl;

        private Integer adverseType;
    }

    /** 日看板（一次返回三个时段 × 全部机位） */
    @Data
    public static class BoardVO {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        private List<BoardCellVO> slot1;

        private List<BoardCellVO> slot2;

        private List<BoardCellVO> slot3;

        /** 当日已排例次（不含取消） */
        private Integer sessionCount;

        /** 当日完成例次 */
        private Integer doneCount;
    }

    /** 分布计数（不良反应类型等） */
    @Data
    public static class TypeCount {
        private Integer type;

        private Integer count;
    }

    /** 机位/时段负荷 */
    @Data
    public static class MachineLoad {
        private String machineNo;

        private Integer count;
    }

    /** 透析工作量统计 */
    @Data
    public static class StatsVO {
        /** 开始日期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /** 结束日期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        private Integer inDialysisPatients;

        private Integer sessionTotal;

        private Integer doneCount;

        private Integer cancelledCount;

        private Integer onMachineCount;

        private Integer scheduledCount;

        private Integer adverseCount;

        /** 例次/在透患者（三甲口径的粗略治疗密度） */
        private BigDecimal sessionsPerPatient;

        private BigDecimal avgUltraMl;

        private Integer avgActualDurationMin;

        private List<TypeCount> adverseTypes;

        private List<MachineLoad> machineLoads;
    }
}
