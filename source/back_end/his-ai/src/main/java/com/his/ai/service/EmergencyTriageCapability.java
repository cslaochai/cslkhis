package com.his.ai.service;

import com.his.ai.dto.EmergencyTriageDTO;
import com.his.ai.vo.EmergencyTriageResultVO;
import java.util.*;

public interface EmergencyTriageCapability {

    EmergencyTriageResultVO suggest(EmergencyTriageDTO dto);
}
