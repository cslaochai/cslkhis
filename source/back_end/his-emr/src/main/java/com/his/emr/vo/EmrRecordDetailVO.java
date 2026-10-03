package com.his.emr.vo;

import lombok.Data;

import java.util.List;
import com.his.emr.vo.BizInspectionApplyVO;
import com.his.emr.vo.BizLaboratoryApplyVO;
import com.his.emr.vo.BizPrescriptionVO;

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
