package com.his.ai.service;

import com.his.ai.dto.InsuranceEvidenceDTO;
import com.his.ai.vo.InsuranceEvidenceVO;

/**
 * 医保审核证据判定（G-07）。
 */
public interface InsuranceEvidenceCapability {

    InsuranceEvidenceVO execute(InsuranceEvidenceDTO dto);
}
