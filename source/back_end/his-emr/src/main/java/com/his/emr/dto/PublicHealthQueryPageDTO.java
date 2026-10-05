package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公卫上报记录查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PublicHealthQueryPageDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 上报类型（1-传染病 2-死因监测 3-慢性病 4-其他）
     */
    private Integer reportType;

    /**
     * 上报状态：1-待审核 2-审核通过 3-审核驳回（字典 his_ph_report_status）
     */
    private Integer reportStatus;

    /**
     * 患者姓名（模糊）——台账页按名字找人是刚需，只有 patientId 等于没法用
     */
    private String patientName;

    /**
     * 单号/诊断模糊
     */
    private String keyword;
}
