package com.his.app.service.impl;

import com.his.app.dto.MiniSurveySubmitDTO;
import com.his.app.service.MiniSurveyService;
import com.his.common.exception.BusinessException;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.service.SurveyFacadeService;
import com.his.spi.vo.PatientSurveyPaperVO;
import com.his.spi.vo.PatientSurveyTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MiniSurveyServiceImpl implements MiniSurveyService {

    private final SurveyFacadeService surveyFacadeService;
    private final PatientFacadeService patientFacadeService;

    @Override
    public List<PatientSurveyTaskVO> pending(Long patientId) {
        requireAccessible(patientId);
        return surveyFacadeService.myPendingTasks(patientId);
    }

    @Override
    public PatientSurveyPaperVO paper(Long patientId, Long dispatchId) {
        requireAccessible(patientId);
        return surveyFacadeService.paper(patientId, dispatchId);
    }

    @Override
    public Long submit(MiniSurveySubmitDTO dto) {
        requireAccessible(dto.getPatientId());
        List<SurveyFacadeService.PatientSurveyAnswerItemVO> answers = new ArrayList<>();
        for (MiniSurveySubmitDTO.Answer answer : dto.getAnswers()) {
            SurveyFacadeService.PatientSurveyAnswerItemVO item = new SurveyFacadeService.PatientSurveyAnswerItemVO();
            item.setItemId(answer.getItemId());
            item.setScore(answer.getScore());
            item.setOptionLabel(answer.getOptionLabel());
            item.setTextValue(answer.getTextValue());
            answers.add(item);
        }
        return surveyFacadeService.submit(dto.getPatientId(), dto.getDispatchId(),
                dto.getAnonymousFlag(), dto.getCommentText(), answers);
    }

    private void requireAccessible(Long patientId) {
        if (patientId == null) {
            throw new BusinessException("就诊人不能为空");
        }
        if (!patientFacadeService.canAccessPatient(patientId)) {
            throw new BusinessException("无权操作该就诊人的满意度问卷");
        }
    }
}
