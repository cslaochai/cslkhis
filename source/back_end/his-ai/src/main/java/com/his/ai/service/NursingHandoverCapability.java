package com.his.ai.service;

import com.his.ai.dto.NursingHandoverDTO;
import com.his.ai.vo.WardHandoverVO;

/**
 * AI 护理交接班摘要（G-13）。
 * <p>病区×班次事实聚合是代码；模型拟 SBAR 摘要草稿，护士终审，不写库。</p>
 */
public interface NursingHandoverCapability {

    WardHandoverVO compose(NursingHandoverDTO dto);
}
