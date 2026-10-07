package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 院感监测 DTO 集合（L10：病例报告卡 / 目标性监测 / 手卫生依从性）。
 */
public class InfectionMonitorDTO {

    // 病例报告卡

    /**
     * 病例报卡 / 补报建卡（leakFlag 由前端漏报入口显式传 1）
     */
    @Data
    public static class CaseUpsert {
        /**
         * 主键
         */
        private Long id;

        /**
         * 患者ID
         */
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        /**
         * 就诊类型（1门诊/2住院）
         */
        @NotNull(message = "就诊类型不能为空")
        @Min(value = 1, message = "就诊类型非法")
        @Max(value = 2, message = "就诊类型非法")
        private Integer visitType;

        /**
         * 门诊就诊ID（visitType=1 必填）
         */
        private Long registId;

        /**
         * 住院记录ID（visitType=2 必填）
         */
        private Long inpId;

        /**
         * 感染来源（1社区感染/2医院感染）
         */
        @NotNull(message = "感染来源不能为空")
        private Integer caseSource;

        /**
         * 感染部位（his_infection_site 码值）
         */
        @NotNull(message = "感染部位不能为空")
        private String infectionSite;

        /**
         * 感染诊断
         */
        @NotBlank(message = "感染诊断不能为空")
        private String infectionDiag;

        private String pathogen;

        private String specimen;

        /**
         * 感染/诊断日期
         */
        @NotNull(message = "感染日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate infectDate;

        /**
         * 漏报补报标志（漏报调查入口显式传 1，普通报卡不传）
         */
        private Integer leakFlag;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 感控办核实（1待核实 → 2已确认 / 3已排除）
     */
    @Data
    public static class CaseAudit {
        /**
         * 主键
         */
        @NotNull(message = "病例ID不能为空")
        private Long id;

        /**
         * 核实结论（2确认/3排除）
         */
        @NotNull(message = "核实结论不能为空")
        @Min(value = 2, message = "核实结论非法")
        @Max(value = 3, message = "核实结论非法")
        private Integer auditResult;

        private String auditRemark;
    }

    /**
     * 病例分页查询
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CaseQueryPage extends PageParam {
        private Integer caseStatus;
        private Integer caseSource;
        private Integer leakFlag;
        /**
         * 患者姓名/病例编号/感染诊断模糊
         */
        private String keyword;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reportTimeStart;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reportTimeEnd;
    }

    // 目标性监测

    /**
     * 监测登记
     */
    @Data
    public static class MonitorAdd {
        /**
         * 患者ID
         */
        @NotNull(message = "患者ID不能为空")
        private Long patientId;

        /**
         * 监测类型（1尿管CAUTI/2血管导管CLABSI/3呼吸机VAP）
         */
        @NotNull(message = "监测类型不能为空")
        @Min(value = 1, message = "监测类型非法")
        @Max(value = 3, message = "监测类型非法")
        private Integer monitorType;

        /**
         * 住院记录ID（监测锚点，必填——导管相关监测都在住院段）
         */
        @NotNull(message = "住院记录ID不能为空")
        private Long inpId;

        /**
         * 置入日期
         */
        @NotNull(message = "置入日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate insertDate;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 每日打卡（导管日）
     */
    @Data
    public static class PunchDaily {
        @NotNull(message = "监测ID不能为空")
        private Long monitorId;

        /**
         * 监测日期（不传=今天）
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate monitorDate;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 拔管（1在管 → 2已拔管）
     */
    @Data
    public static class MonitorRemove {
        @NotNull(message = "监测ID不能为空")
        private Long monitorId;

        /**
         * 拔除日期
         */
        @NotNull(message = "拔除日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate removeDate;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 感染确认（infectionFlag=1，不改在管状态）
     */
    @Data
    public static class ConfirmInfection {
        @NotNull(message = "监测ID不能为空")
        private Long monitorId;

        /**
         * 感染日期
         */
        @NotNull(message = "感染日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate infectionDate;

        /**
         * 感染部位（his_infection_site 码值）
         */
        @NotNull(message = "感染部位不能为空")
        private String infectionSite;

        /**
         * 感染诊断
         */
        @NotBlank(message = "感染诊断不能为空")
        private String infectionDiag;
    }

    /**
     * 监测分页查询
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class MonitorQueryPage extends PageParam {
        /**
         * 监测类型
         */
        private Integer monitorType;
        /**
         * 状态
         */
        private Integer status;
        /**
         * 感染确认
         */
        private Integer infectionFlag;
        /**
         * 关键字
         */
        private String keyword;
    }

    // 手卫生依从性观察

    /**
     * 观察登记（只增不改）
     */
    @Data
    public static class HandObsAdd {
        @NotNull(message = "观察日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate obsDate;

        /**
         * 监测科室ID
         */
        @NotNull(message = "科室不能为空")
        private Long deptId;

        /**
         * 观察对象（1医生/2护士/3工勤其他）
         */
        @NotNull(message = "观察对象不能为空")
        @Min(value = 1, message = "观察对象非法")
        @Max(value = 3, message = "观察对象非法")
        private Integer obsObject;

        @NotNull(message = "时机数不能为空")
        @Min(value = 1, message = "时机数至少为 1")
        private Integer opportunityCount;

        @NotNull(message = "执行数不能为空")
        @Min(value = 0, message = "执行数不能为负")
        private Integer complyCount;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 观察记录分页查询
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class HandObsQueryPage extends PageParam {
        /**
         * 监测科室ID
         */
        private Long deptId;
        private Integer obsObject;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate obsDateStart;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate obsDateEnd;
    }

    /**
     * 观察统计查询（按科室/月份聚合的口径窗口）
     */
    @Data
    public static class HandObsStatsQuery {
        /**
         * 不传=近 30 天
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate obsDateStart;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate obsDateEnd;
    }
}
