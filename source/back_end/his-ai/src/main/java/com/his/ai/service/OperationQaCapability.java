package com.his.ai.service;

import com.his.ai.dto.OperationQaAskDTO;
import com.his.ai.vo.OperationQaResultVO;
import com.his.ai.vo.OperationSchemaVO;

import java.util.List;

public interface OperationQaCapability {

    OperationQaResultVO ask(OperationQaAskDTO dto);

    List<OperationSchemaVO> schema();
}
