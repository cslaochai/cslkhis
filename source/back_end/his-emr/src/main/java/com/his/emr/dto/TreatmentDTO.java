package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 门诊治疗站入参（治疗申请 / 按次流水）。
 *
 * <p>日期入参用空格/短横分隔的 pattern 宽进（AGENTS §3）：LocalDate 只认 yyyy-MM-dd，
 * 前端 el-date-picker 的 value-format 必须与之一致。
 */
public class TreatmentDTO {

    // 申请单

    @Data
    public static class ApplyQuery {
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 20;
        /** 关键字 */
        private String keyword;
        /** 患者ID */
        private Long patientId;
        private Long registId;
        private Integer applyStatus;
        private Long treatmentItemId;

        /** 开始日期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /** 结束日期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    @Data
    public static class ApplyUpsert {
        /** 空=新开疗程；非空=改期/改次数（仅未打过任何一次卡时允许） */
        private Long applyId;

        @NotNull(message = "挂号ID不能为空（门诊治疗必须挂在一次就诊上）")
        private Long registId;

        @NotNull(message = "治疗项目不能为空")
        private Long treatmentItemId;

        @NotNull(message = "疗程总次数不能为空")
        @Min(value = 1, message = "疗程总次数至少 1 次")
        @Max(value = 60, message = "疗程总次数不得超过 60 次")
        private Integer totalTimes;

        /** 相邻两次间隔天数，默认 1（每日一次） */
        @Min(value = 1, message = "间隔天数至少 1 天")
        @Max(value = 30, message = "间隔天数不得超过 30 天")
        private Integer intervalDays;

        /** 开始日期 */
        @NotNull(message = "疗程开始日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /** 备注 */
        private String remark;
    }

    @Data
    public static class ApplyIdOnly {
        @NotNull(message = "治疗申请单不能为空")
        private Long applyId;
    }

    @Data
    public static class ApplyCancel {
        @NotNull(message = "治疗申请单不能为空")
        private Long applyId;

        /** 原因 */
        @NotBlank(message = "取消原因不能为空")
        private String reason;
    }

    // 按次流水

    @Data
    public static class ExecQuery {
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 20;
        /** 关键字 */
        private String keyword;
        private Long applyId;
        /** 患者ID */
        private Long patientId;
        private Integer execStatus;
        private Integer chargeStatus;

        /** 只看"逾期未打卡"（exec_status=0 且 plan_date < 今天） */
        private Boolean overdueOnly;

        /** 只看"已执行但钱没记上"（exec_status=1 且 charge_status in (0,2)） */
        private Boolean unbilledOnly;

        /** 计划日期（治疗台用它锁定"今天"，只影响查询，不参与任何写动作） */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate planDate;

        /** 开始日期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /** 结束日期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    @Data
    public static class ExecIdOnly {
        @NotNull(message = "执行流水不能为空")
        private Long recordId;
    }

    @Data
    public static class ExecReschedule {
        @NotNull(message = "执行流水不能为空")
        private Long recordId;

        @NotNull(message = "改期后的计划日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate planDate;

        /** 原因 */
        @NotBlank(message = "改期原因不能为空")
        private String reason;
    }

    /** 打卡：这一次实际做了，顺带记录患者反应 */
    @Data
    public static class ExecExecute {
        @NotNull(message = "执行流水不能为空")
        private Long recordId;

        /** 0-异常 1-正常，默认 1 */
        private Integer recordStatus;

        private String result;

        /** 备注 */
        private String remark;
    }

    /** 计费补记：只针对"已执行但未计费/计费失败"的行 */
    @Data
    public static class ExecRetryCharge {
        @NotNull(message = "执行流水不能为空")
        private Long recordId;
    }
}
