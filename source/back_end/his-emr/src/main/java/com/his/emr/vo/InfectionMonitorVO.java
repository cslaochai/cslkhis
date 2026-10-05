package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 院感监测 VO 集合（L10）。ID 全部 ToStringSerializer，前端不要 Number()。
 */
public class InfectionMonitorVO {

    // 病例报告卡

    @Data
    public static class CaseRow {
        /**
         * 主键
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private String caseNo;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 患者编号（快照）
         */
        private String patientNo;
        /**
         * 患者姓名（快照）
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
        private Integer visitType;
        private String visitTypeText;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long registId;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long inpId;

        /**
         * 监测科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        /**
         * 监测科室（快照）
         */
        private String deptName;
        private Integer caseSource;
        private String caseSourceText;
        /**
         * 感染部位
         */
        private String infectionSite;
        private String infectionSiteText;
        /**
         * 感染诊断
         */
        private String infectionDiag;
        private String pathogen;
        private String specimen;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate infectDate;

        private Integer caseStatus;
        private String caseStatusText;
        private Integer leakFlag;
        private String reportName;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reportTime;

        private String auditName;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime auditTime;

        private String auditRemark;
        /**
         * 备注
         */
        private String remark;
        /**
         * 创建时间
         */
        private String createTime;
    }

    @Data
    public static class CaseStats {
        private long pendingAudit = 0;
        private long confirmed = 0;
        private long excluded = 0;
        private long leakResubmit = 0;
        private long hospitalInfection = 0;
        private long todayNew = 0;
    }

    // 目标性监测

    @Data
    public static class MonitorRow {
        /**
         * 主键
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 监测编号
         */
        private String monitorNo;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 患者编号（快照）
         */
        private String patientNo;
        /**
         * 患者姓名（快照）
         */
        private String patientName;
        /**
         * 监测类型
         */
        private Integer monitorType;
        private String monitorTypeText;

        /**
         * 监测科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        /**
         * 监测科室（快照）
         */
        private String deptName;

        /**
         * 置入日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate insertDate;

        /**
         * 拔除日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate removeDate;

        /**
         * 状态
         */
        private Integer status;
        /**
         * 状态文本
         */
        private String statusText;
        /**
         * 感染确认
         */
        private Integer infectionFlag;

        /**
         * 感染日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate infectionDate;

        /**
         * 感染部位
         */
        private String infectionSite;
        private String infectionSiteText;
        /**
         * 感染诊断
         */
        private String infectionDiag;

        /**
         * 导管日（有效打卡数，服务端算好）
         */
        private Integer catheterDays;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 每日打卡明细行
     */
    @Data
    public static class DailyRow {
        /**
         * 主键
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long monitorId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate monitorDate;

        private String recorderName;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime recordTime;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 监测统计：按类型分组（导管日/例次/感染率）+ 总览
     */
    @Data
    public static class MonitorStats {
        private long inCatheter = 0;
        private long removed = 0;
        private long infectionConfirmed = 0;
        private long catheterDays = 0;
        /**
         * 感染率（‰）= 感染例次 / 导管日 × 1000，导管日为 0 时给 0
         */
        private double infectionRate;
        private List<TypeGroup> byType;
    }

    @Data
    public static class TypeGroup {
        /**
         * 监测类型
         */
        private Integer monitorType;
        private String monitorTypeText;
        private long registered = 0;
        private long catheterDays = 0;
        private long infectionCases = 0;
        private double infectionRate;
    }

    // 手卫生依从性

    @Data
    public static class HandObsRow {
        /**
         * 主键
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate obsDate;

        /**
         * 监测科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        /**
         * 监测科室（快照）
         */
        private String deptName;
        private Integer obsObject;
        private String obsObjectText;
        private Integer opportunityCount;
        private Integer complyCount;
        /**
         * 单条依从率（%），服务端算好
         */
        private double complyRate;
        private String observerName;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime obsTime;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    public static class HandObsStats {
        /**
         * 汇总时机数
         */
        private long totalOpportunity = 0;
        /**
         * 汇总执行数
         */
        private long totalComply = 0;
        /**
         * 依从率（%）= 执行 / 时机 × 100
         */
        private double complyRate;
        private long recordCount = 0;
        /**
         * 按对象分组
         */
        private List<ObjectGroup> byObject;
    }

    @Data
    public static class ObjectGroup {
        private Integer obsObject;
        private String obsObjectText;
        private long opportunity = 0;
        private long comply = 0;
        private double complyRate;
    }
}
