package com.his.ai.service;

import com.his.ai.dto.EmrQcExecuteDTO;
import com.his.ai.vo.EmrQcResultVO;

public interface EmrQcCapability {

    EmrQcResultVO execute(EmrQcExecuteDTO dto);
}
