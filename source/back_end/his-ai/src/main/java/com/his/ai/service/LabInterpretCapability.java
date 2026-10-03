package com.his.ai.service;

import com.his.ai.dto.LabInterpretExecuteDTO;
import com.his.ai.vo.LabInterpretResultVO;
import java.util.*;

public interface LabInterpretCapability {

    LabInterpretResultVO execute(LabInterpretExecuteDTO dto);
}
