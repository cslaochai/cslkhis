package com.his.app.service;

import com.his.app.dto.MiniSurveySubmitDTO;
import com.his.spi.vo.PatientSurveyPaperVO;
import com.his.spi.vo.PatientSurveyTaskVO;

import java.util.List;

/**
 * 患者端满意度评价。
 */
public interface MiniSurveyService {

    /**
     * 我的待填问卷
     */
    List<PatientSurveyTaskVO> pending(Long patientId);

    /**
     * 问卷卷面
     */
    PatientSurveyPaperVO paper(Long patientId, Long dispatchId);

    /**
     * 提交答卷（返回答卷ID）
     */
    Long submit(MiniSurveySubmitDTO dto);
}
