package com.his.ai.service;

import com.his.ai.dto.AiCallDTO;
import com.his.ai.support.*;
import java.util.Optional;

public interface AiExecutionService {

    <T> Optional<T> call(AiCallDTO call, Class<T> resultType);

    String degradeReasonOf(String capabilityKey);
}
