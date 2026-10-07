package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 门诊治疗站出参。
 */
public class TreatmentVO {

    @Data
    public static class ApplyVO {

        @JsonSerialize(using = ToStringSerializer.class)
        private Long applyId;

        private String applyNo;

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
        private Long registId;

        private String registNo;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long treatmentItemId;

        private String itemCode;
        /**
         * 项目名称
         */
        private String itemName;
        /**
         * 项目类型
         */
        private Integer itemType;
        private String itemTypeText;
        /**
         * 单价
         */
        private BigDecimal price;

        private Integer totalTimes;

        private Integer doneTimes;
        /**
         * 如「3/7」，页面直接显示
         */
        private String progressText;
        /**
         * 剩余待执行次数（不含已取消）
         */
        private Integer pendingTimes;
        /**
         * 疗程计划总额 = price × totalTimes，NULL 单价时为 NULL（不显示成 0 元）
         */
        private BigDecimal planAmount;

        /**
         * 开始日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        private Integer intervalDays;

        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long execDeptId;

        private String execDeptName;

        /**
         * 医生姓名
         */
        private String doctorName;

        /**
         * 申请时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime applyTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime executeTime;

        private Integer applyStatus;

        private String applyStatusText;
        /**
         * 疗程是否已做完（doneTimes ≥ totalTimes）
         */
        private Boolean finished;
        /**
         * 备注
         */
        private String remark;
    }

    @Data
    public static class ApplyDetailVO {
        private ApplyVO apply;
        private List<ExecVO> execList;
    }

    @Data
    public static class ExecVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long recordId;
        private String recordNo;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long applyId;
        private String applyNo;

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

        private Integer execSeq;
        /**
         * 项目名称
         */
        private String itemName;
        /**
         * 项目类型
         */
        private Integer itemType;
        private String itemTypeText;
        /**
         * 单价
         */
        private BigDecimal price;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate planDate;

        private Integer execStatus;
        private String execStatusText;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime executeTime;
        private String executorName;
        private Integer recordStatus;
        private String recordStatusText;
        private String result;

        private Integer chargeStatus;
        private String chargeStatusText;
        /**
         * 记账单号
         */
        private String feeNo;
        /**
         * 这一次治疗落下的那条 L1 记账行ID（对账时从流水直接跳到记账行）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long feeRecordId;
        private BigDecimal chargeAmount;
        private String chargeFailReason;

        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long execDeptId;
        private String execDeptName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 计划日期已过但还没打卡
         */
        private Boolean overdue;
        /**
         * 后端算好的"这一行现在能不能打卡"，与 executeExec 的校验同一口径
         */
        private Boolean canExecute;
        /**
         * 不能打卡时的一句话原因（页面 tooltip 用，避免只禁用不给理由）
         */
        private String cannotExecuteReason;
        /**
         * 已执行但未计费/计费失败 → 可补记
         */
        private Boolean canRetryCharge;

        /**
         * 打卡/补记这一次计费结果的一句话提示（只在写接口返回值里出现，查询恒为 NULL）
         */
        private String chargeNotice;
    }

    @Data
    public static class StatusCountVO {
        private String key;
        private String label;
        private Long count;
    }

    @Data
    public static class StatsVO {
        /**
         * 今天在办疗程数
         */
        private Long runningApplies;
        private Long todayPlan;
        private Long todayDone;
        private Long todayPending;
        /**
         * 逾期未打卡（跨天累计，治疗台第一眼要看到的数）
         */
        private Long overduePending;
        /**
         * 已执行但未计费/计费失败
         */
        private Long unbilled;
        private Long chargeFail;
        /**
         * 今日已产生治疗费（按流水 charge_amount 求和）
         */
        private BigDecimal todayAmount;
    }

    @Data
    public static class ItemSelectListVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long itemId;
        private String itemCode;
        /**
         * 项目名称
         */
        private String itemName;
        /**
         * 项目类型
         */
        private Integer itemType;
        private String itemTypeText;
        /**
         * 单价
         */
        private BigDecimal price;
        private Integer duration;
        private String usageMethod;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long execDeptId;
        private String execDeptName;
    }
}
