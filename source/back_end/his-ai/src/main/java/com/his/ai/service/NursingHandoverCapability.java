package com.his.ai.service;

import com.his.ai.dto.NursingHandoverDTO;
import com.his.ai.vo.WardHandoverVO;

/**
 * AI 护理交接班摘要（G-13）。
 */
public interface NursingHandoverCapability {

    WardHandoverVO compose(NursingHandoverDTO dto);
}
