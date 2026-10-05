package com.his.ai.service;

import com.his.ai.dto.EmrDraftDTO;
import com.his.ai.vo.EmrDraftResultVO;

public interface EmrDraftCapability {

    EmrDraftResultVO execute(EmrDraftDTO dto);
}
