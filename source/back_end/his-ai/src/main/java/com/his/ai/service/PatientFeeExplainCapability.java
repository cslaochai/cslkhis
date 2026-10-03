package com.his.ai.service;

import com.his.ai.dto.PatientFeeExplainDTO;
import com.his.ai.vo.PatientFeeExplainVO;

/**
 * 患者端费用解释能力。
 * <p>
 * 回答「这笔钱怎么算的、为什么我要自付这么多」。答案来自账单明细的
 * 医保目录类别拆分，是确定性计算；模型只把数字串成一句话。
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
