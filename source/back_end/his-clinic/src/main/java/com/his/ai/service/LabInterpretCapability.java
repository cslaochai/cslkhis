package com.his.ai.service;

import com.his.ai.dto.LabInterpretExecuteDTO;
import com.his.ai.vo.LabInterpretResultVO;

public interface LabInterpretCapability {

    LabInterpretResultVO execute(LabInterpretExecuteDTO dto);
}
