package com.his.ai.service;

import com.his.ai.dto.EmergencyTriageDTO;
import com.his.ai.vo.EmergencyTriageResultVO;

public interface EmergencyTriageCapability {

    EmergencyTriageResultVO suggest(EmergencyTriageDTO dto);
}
