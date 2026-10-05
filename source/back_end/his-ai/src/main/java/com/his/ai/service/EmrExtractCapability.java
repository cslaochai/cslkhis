package com.his.ai.service;

import com.his.ai.dto.EmrExtractDTO;
import com.his.ai.vo.EmrExtractResultVO;

public interface EmrExtractCapability {

    EmrExtractResultVO execute(EmrExtractDTO dto);
}
