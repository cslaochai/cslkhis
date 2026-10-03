package com.his.ai.service;

import com.his.ai.dto.Icd10PredictDTO;
import com.his.ai.vo.Icd10PredictVO;
import java.util.*;

public interface Icd10Capability {

    Icd10PredictVO predict(Icd10PredictDTO dto);
}
