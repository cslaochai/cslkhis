package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 随访任务 DTO 集合。
 */
public class FollowupTaskDTO {

    /** 新建 / 修改随访任务（id 为空=新建；仅「待随访」可改） */
    @Data
    public static class Upsert implements Serializable {

        /** 主键（修改时必填） */
        private Long id;

        /** 患者ID */
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        /** 随访类型（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访） */
        @NotNull(message = "随访类型不能为空")
        private Integer followupType;

        /** 计划随访时间 */
        @NotNull(message = "计划随访时间不能为空")
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime followupTime;

        /** 随访内容 */
        @NotBlank(message = "随访内容不能为空")
        private String followupContent;

        /** 联系电话（不填自动取患者档案电话） */
        private String phone;

        /** 备注 */
        private String remark;
    }

    /** 按出院记录一键生成随访计划 */
    @Data
    public static class FromDischarge implements Serializable {

        @NotNull(message = "出院记录ID不能为空")
        private Long dischargeId;

        /** 随访类型（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访） */
        private Integer followupType;

        /** 计划随访时间 = 出院时间 + N 天（默认 7） */
        private Integer daysOffset;

        /** 随访内容覆盖（默认按出院诊断自动生成） */
        private String followupContent;
    }

    /**
     * 由随访任务生成复诊号（复诊来源 4-随访计划复诊）
     *
     * <p>原病历要单独传：任务表只记了患者与随访内容，没有「哪一次就诊」，
     * 而收费策略与间隔天数全部以原病历的就诊日/科室/医生为基准 —— 缺它判不出价。
     */
    @Data
    public static class CreateRevisit implements Serializable {

        @NotNull(message = "随访任务ID不能为空")
        private Long taskId;

        /** 复诊引用的原病历ID */
        @NotNull(message = "原病历不能为空")
        private Long revisitRecordId;

        @NotNull(message = "请选择号源")
        private Long scheduleId;

        /** 时间片段ID（可选；不传按排班主表扣号源） */
        private Long slotId;

        /** 结算方式（1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-商业保险），默认自费 */
        private Integer settlementType;
    }
}
