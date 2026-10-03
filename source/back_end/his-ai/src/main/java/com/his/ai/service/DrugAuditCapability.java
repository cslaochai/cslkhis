package com.his.ai.service;

import com.his.ai.dto.DrugAuditExecuteDTO;
import com.his.ai.vo.DrugAuditResultVO;
import java.util.*;

public interface DrugAuditCapability {

    DrugAuditResultVO execute(DrugAuditExecuteDTO dto);
}
