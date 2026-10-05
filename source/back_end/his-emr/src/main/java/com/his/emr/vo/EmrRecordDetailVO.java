package com.his.emr.vo;

import lombok.Data;

import java.util.List;

/**
 * 病历详情出参（病历 + 处方 + 检查申请 + 检验申请）
 */
@Data
public class EmrRecordDetailVO {
    /**
     * 病历信息
     */
    private BizMedicalRecordVO record;

    /**
     * 处方列表
     */
    private List<BizPrescriptionVO> prescriptions;

    /**
     * 检查申请列表
     */
    private List<BizInspectionApplyVO> inspectionApplies;

    /**
     * 检验申请列表
     */
    private List<BizLaboratoryApplyVO> laboratoryApplies;
}
