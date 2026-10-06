package com.his.medicaltech.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import com.his.common.validation.InEnum;
import com.his.medicaltech.enums.DialysisPatientStatusEnum;
import com.his.medicaltech.enums.DialysisTimeSlotEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 血液净化（透析）入参。
 *
 * <p>原因/描述类字段只在服务端截列宽（见 DialysisService），此处不再挂 {@code @Size}，
 * 否则入参层 400 会抢在服务端截断之前（AGENTS.md 第 3 条）。
 */
public class DialysisDTO {

    /** 透析档案分页查询 */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ArchiveQuery extends PageParam {
        private String dialysisNo;

        /** 患者姓名（快照） */
        private String patientName;

        private Integer accessType;

        private Integer status;
    }

    /** 透析档案新增/修改（patientId 唯一，一人一档） */
    @Data
    public static class ArchiveUpsert {
        private Long id;

        /** 患者ID */
        @NotNull(message = "请选择患者")
        private Long patientId;

        @NotNull(message = "首次透析日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate firstDialysisDate;

        private String cause;

        @NotNull(message = "血管通路不能为空")
        private Integer accessType;

        private String accessSite;

        private Integer dialysisFreq;

        /** 备注 */
        private String remark;
    }

    /** 档案状态变更（1在透 / 2暂停 / 3退出；2、3 原因必填） */
    @Data
    public static class ArchiveStatus {
        @NotNull(message = "档案ID不能为空")
        private Long id;

        @NotNull(message = "目标状态不能为空")
        @InEnum(value = DialysisPatientStatusEnum.class, message = "目标状态取值不合法（1-在透 2-暂停 3-退出）")
        private Integer status;

        /** 原因 */
        private String reason;
    }

    /** 透析处方新增/修改（同一档案同时只允许一张有效） */
    @Data
    public static class PrescriptionUpsert {
        private Long id;

        /** 透析档案ID */
        @NotNull(message = "透析档案不能为空")
        private Long archiveId;

        /** 干体重 kg */
        @NotNull(message = "干体重不能为空")
        private BigDecimal dryWeight;

        /** 处方透析时长分钟（快照） */
        private Integer durationMin;

        /** 处方血流量（快照） */
        private Integer bloodFlow;

        /** 透析器（快照） */
        private Integer dialyzer;

        /** 抗凝方式（快照） */
        private Integer anticoagulant;

        private String anticoagDose;

        private BigDecimal targetUltraMl;

        /** 开始日期 */
        @NotNull(message = "处方生效日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /** 备注 */
        private String remark;
    }

    /** 处方停用 */
    @Data
    public static class PrescriptionStop {
        @NotNull(message = "处方ID不能为空")
        private Long id;

        /** 原因 */
        private String reason;
    }

    /** 机位维护 */
    @Data
    public static class MachineUpsert {
        private Long id;

        /** 机位号（快照） */
        @NotBlank(message = "机位号不能为空")
        private String machineNo;

        private String roomName;

        @NotNull(message = "机位状态不能为空")
        private Integer status;

        /** 备注 */
        private String remark;
    }

    /** 机位分页查询 */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class MachineQuery extends PageParam {
        /** 机位号（快照） */
        private String machineNo;

        private String roomName;

        private Integer status;
    }

    /** 透析单分页查询 */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SessionQuery extends PageParam {
        /** 透析单号 */
        private String sessionNo;

        /** 患者姓名（快照） */
        private String patientName;

        /** 开始日期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /** 结束日期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        /** 时段（1-上午 2-下午 3-夜间） */
        private Integer timeSlot;

        /** 机位ID */
        private Long machineId;

        /** 透析档案ID */
        private Long archiveId;

        private Integer status;
    }

    /** 排班（占机位，处方取当前有效处方快照） */
    @Data
    public static class Schedule {
        /** 透析档案ID */
        @NotNull(message = "透析档案不能为空")
        private Long archiveId;

        /** 透析日期 */
        @NotNull(message = "透析日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dialysisDate;

        /** 时段（1-上午 2-下午 3-夜间） */
        @NotNull(message = "时段不能为空")
        @InEnum(value = DialysisTimeSlotEnum.class, message = "时段取值不合法（1-上午 2-下午 3-夜间）")
        private Integer timeSlot;

        /** 机位ID */
        @NotNull(message = "机位不能为空")
        private Long machineId;

        /** 备注 */
        private String remark;
    }

    /** 排班改期/改机位（仅在径=已排班可改） */
    @Data
    public static class Reschedule {
        @NotNull(message = "透析单不能为空")
        private Long id;

        /** 透析日期 */
        @NotNull(message = "透析日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dialysisDate;

        /** 时段（1-上午 2-下午 3-夜间） */
        @NotNull(message = "时段不能为空")
        @InEnum(value = DialysisTimeSlotEnum.class, message = "时段取值不合法（1-上午 2-下午 3-夜间）")
        private Integer timeSlot;

        /** 机位ID */
        @NotNull(message = "机位不能为空")
        private Long machineId;

        /** 备注 */
        private String remark;
    }

    /** 上机（透前体重 + 通路评估必填） */
    @Data
    public static class SessionStart {
        @NotNull(message = "透析单不能为空")
        private Long id;

        /** 透前体重 kg */
        @NotNull(message = "透前体重不能为空")
        private BigDecimal beforeWeight;

        /** 通路评估 */
        @NotBlank(message = "通路评估不能为空")
        private String accessCheck;

        /** 上机时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime onTime;
    }

    /** 下机（透后体重必填，超滤量与实际时长服务端回算） */
    @Data
    public static class SessionFinish {
        @NotNull(message = "透析单不能为空")
        private Long id;

        /** 透后体重 kg */
        @NotNull(message = "透后体重不能为空")
        private BigDecimal afterWeight;

        /** 下机时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime offTime;

        /** 备注 */
        private String remark;
    }

    /** 透析中/下机后补登不良反应 */
    @Data
    public static class AdverseUpsert {
        @NotNull(message = "透析单不能为空")
        private Long id;

        /** 不良反应类型（，空=无） */
        @NotNull(message = "不良反应类型不能为空")
        private Integer adverseType;

        /** 不良反应处置描述 */
        private String adverseDesc;
    }

    /** 取消排班（原因必填，释放机位） */
    @Data
    public static class SessionCancel {
        @NotNull(message = "透析单不能为空")
        private Long id;

        /** 原因 */
        @NotBlank(message = "取消原因不能为空")
        private String reason;
    }

    /** 工作量统计区间 */
    @Data
    public static class StatsQuery {
        /** 开始日期 */
        @NotNull(message = "开始日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /** 结束日期 */
        @NotNull(message = "结束日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }
}
