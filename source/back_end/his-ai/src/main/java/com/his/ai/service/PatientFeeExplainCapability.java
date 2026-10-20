package com.his.ai.service;

import com.his.ai.dto.PatientFeeExplainDTO;
import com.his.ai.vo.PatientFeeExplainVO;

/**
 * 患者端费用解释能力。
 */
public interface PatientFeeExplainCapability {

    /**
     * 解释一张结算账单。
     *
     * @param dto 账单ID（患者身份服务端取）
     * @return 解释结果
     */
    PatientFeeExplainVO execute(PatientFeeExplainDTO dto);
}
