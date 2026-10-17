package com.his.ai.service;

import com.his.ai.dto.Icd10PredictDTO;
import com.his.ai.vo.Icd10PredictResultVO;

public interface Icd10Capability {

    Icd10PredictResultVO predict(Icd10PredictDTO dto);
}
