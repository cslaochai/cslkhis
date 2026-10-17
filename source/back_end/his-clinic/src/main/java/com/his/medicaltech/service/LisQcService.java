package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.LisQcDTO;
import com.his.medicaltech.vo.LisQcVO;

public interface LisQcService {

    PageResult<LisQcVO.PlanVO> planPage(LisQcDTO.PlanQuery q);

    String planUpsert(LisQcDTO.PlanUpsert dto);

    void planToggle(Long planId, Integer status);

    LisQcVO.RecordVO inputResult(LisQcDTO.ResultInput dto);

    PageResult<LisQcVO.RecordVO> recordPage(LisQcDTO.RecordQuery q);

    void handle(LisQcDTO.Handle dto);

    void review(LisQcDTO.Review dto);

    LisQcVO.StatsVO stats();
}
