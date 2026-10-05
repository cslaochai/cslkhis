package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 院感目标性监测登记（导管相关三类型）。
 * 感染确认（infectionFlag）与在管状态（status）独立：感染确认不改在管，导管日统计才完整。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_infection_monitor")
public class BizInfectionMonitor extends BaseEntity {

    /**
     * 监测编号（IMON+yyyyMMdd+4位）
     */
    private String monitorNo;

    /**
     * 患者ID
     */
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
     * 监测类型（1尿管CAUTI/2血管导管CLABSI/3呼吸机VAP）
     */
    private Integer monitorType;

    /**
     * 监测科室ID
     */
    private Long deptId;

    /**
     * 监测科室（快照）
     */
    private String deptName;

    /**
     * 置入日期
     */
    private LocalDate insertDate;

    /**
     * 拔除日期（拔管时回填）
     */
    private LocalDate removeDate;

    /**
     * 状态（1在管/2已拔管）
     */
    private Integer status;

    /**
     * 感染确认（0否/1是）
     */
    private Integer infectionFlag;

    /**
     * 感染日期（确认感染时必填）
     */
    private LocalDate infectionDate;

    /**
     * 感染部位（his_infection_site）
     */
    private String infectionSite;

    /**
     * 感染诊断
     */
    private String infectionDiag;
}
