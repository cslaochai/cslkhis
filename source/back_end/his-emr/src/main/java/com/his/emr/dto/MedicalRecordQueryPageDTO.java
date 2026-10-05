package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 病历分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MedicalRecordQueryPageDTO extends PageParam {

    /**
     * 关键字（模糊匹配：患者姓名 / 病历号 / 挂号单号）
     */
    private String keyword;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 挂号ID
     */
    private Long registId;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 就诊日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 就诊日期-起（含）。与 visitDate 二选一：visitDate 精确某天，本组用于区间筛选。
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDateStart;

    /**
     * 就诊日期-止（含）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDateEnd;

    /**
     * 病历状态（1-草稿 2-已提交 3-已归档 4-已作废）
     */
    private Integer recordStatus;

    /**
     * 审核状态（0-待提交 1-待审核 2-审核通过 3-审核驳回）
     */
    private Integer reviewStatus;
}
