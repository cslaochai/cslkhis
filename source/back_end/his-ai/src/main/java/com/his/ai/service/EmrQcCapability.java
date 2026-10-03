package com.his.ai.service;

import com.his.ai.dto.EmrQcExecuteDTO;
import com.his.ai.vo.EmrQcResultVO;
import java.util.*;

public interface EmrQcCapability {

    EmrQcResultVO execute(EmrQcExecuteDTO dto);
}
