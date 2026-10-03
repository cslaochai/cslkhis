package com.his.ai.service;

import com.his.ai.dto.EmrExtractDTO;
import com.his.ai.vo.EmrExtractResultVO;
import java.util.*;

public interface EmrExtractCapability {

    EmrExtractResultVO execute(EmrExtractDTO dto);
}
