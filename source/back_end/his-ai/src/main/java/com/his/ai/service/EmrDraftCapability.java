package com.his.ai.service;

import com.his.ai.dto.EmrDraftDTO;
import com.his.ai.vo.EmrDraftResultVO;
import java.util.*;

public interface EmrDraftCapability {

    EmrDraftResultVO execute(EmrDraftDTO dto);
}
