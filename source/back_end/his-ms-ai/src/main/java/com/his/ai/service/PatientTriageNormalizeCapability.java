package com.his.ai.service;

import com.his.ai.dto.PatientTriageNormalizeDTO;
import com.his.ai.vo.PatientTriageNormalizeVO;

/**
 * 患者端导诊口语归一。
 */
public interface PatientTriageNormalizeCapability {

    /**
     * 归一一句口语主诉。
     *
     * @param dto 患者原话
     * @return 归一结果；模型不可用仅意味着"用原话查"，不影响导诊可用
     */
    PatientTriageNormalizeVO execute(PatientTriageNormalizeDTO dto);
}
