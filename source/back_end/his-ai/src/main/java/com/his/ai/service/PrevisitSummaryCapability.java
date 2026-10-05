package com.his.ai.service;

import com.his.ai.dto.PrevisitSummaryDTO;
import com.his.ai.vo.PrevisitSummaryVO;

/**
 * 预问诊病史摘要能力（G-05）
 */
public interface PrevisitSummaryCapability {

    PrevisitSummaryVO execute(PrevisitSummaryDTO dto);
}
