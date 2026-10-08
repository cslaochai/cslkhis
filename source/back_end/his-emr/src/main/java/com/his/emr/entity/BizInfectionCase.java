package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 院感病例报告卡：临床报卡 → 感控办核实（确认/排除）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_infection_case")
public class BizInfectionCase extends BaseEntity {

    /**
     * 病例编号（ICASE+yyyyMMdd+4位）
     */
    private String caseNo;

    /**
     * 患者ID
     */
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
     * 性别（快照，性别字典）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 就诊类型（1门诊/2住院）
     */
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
     * 发现科室ID
     */
    private Long deptId;

    /**
     * 发现科室
     */
    private String deptName;

    /**
     * 感染来源（1社区感染/2医院感染）
     */
    private Integer caseSource;

    /**
     * 感染部位（his_infection_site）
     */
    private String infectionSite;

    /**
     * 感染诊断
     */
    private String infectionDiag;

    /**
     * 病原菌
     */
    private String pathogen;

    /**
     * 标本来源
     */
    private String specimen;

    /**
     * 感染/诊断日期
     */
    private LocalDate infectDate;

    /**
     * 状态（1待核实/2已确认/3已排除）
     */
    private Integer caseStatus;

    /**
     * 漏报标志（1=漏报调查发现后补报）
     */
    private Integer leakFlag;

    /**
     * 上报人ID
     */
    private Long reportBy;

    /**
     * 上报人姓名
     */
    private String reportName;

    /**
     * 上报时间
     */
    private LocalDateTime reportTime;

    /**
     * 核实人（感控办）
     */
    private String auditName;

    /**
     * 核实时间
     */
    private LocalDateTime auditTime;

    /**
     * 核实意见
     */
    private String auditRemark;
}
