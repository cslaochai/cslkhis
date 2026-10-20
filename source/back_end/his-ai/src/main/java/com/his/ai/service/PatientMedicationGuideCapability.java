package com.his.ai.service;

import com.his.ai.dto.PatientMedicationGuideDTO;
import com.his.ai.vo.PatientMedicationGuideVO;

/**
 * 患者端用药说明（这盒药怎么吃）。
 */
public interface PatientMedicationGuideCapability {

    /**
     * 生成一张处方的用药说明。
     *
     * @param dto 处方ID（患者身份服务端取）
     * @return 用药说明
     */
    PatientMedicationGuideVO execute(PatientMedicationGuideDTO dto);
}
