package com.his.clinical.spi;

import com.his.clinical.dto.SurveyAnswerUpsertDTO;
import com.his.clinical.service.SurveyService;
import com.his.spi.service.SurveyFacadeService;
import com.his.spi.vo.PatientSurveyPaperVO;
import com.his.spi.vo.PatientSurveyTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 满意度患者填报端口实现。
 *
 * <p>已核对依赖图：SurveyServiceImpl 只依赖 bizSurvey*Mapper / DisputeService / DeptScopeService /
 * RedisSequenceService，其中 DisputeServiceImpl 不回指 SurveyService，不构成 Bean 环。</p>
 */
@Service
@RequiredArgsConstructor
public class SurveyFacadeServiceImpl implements SurveyFacadeService {

    private final SurveyService surveyService;

    @Override
    public List<PatientSurveyTaskVO> myPendingTasks(Long patientId) {
        return surveyService.myPendingTasks(patientId);
    }

    @Override
    public PatientSurveyPaperVO paper(Long patientId, Long dispatchId) {
        return surveyService.paperForPatient(patientId, dispatchId);
    }

    @Override
    public Long submit(Long patientId, Long dispatchId, Integer anonymousFlag, String commentText,
                       List<PatientSurveyAnswerItemVO> answers) {
        List<SurveyAnswerUpsertDTO.Item> items = new ArrayList<>();
        if (answers != null) {
            for (PatientSurveyAnswerItemVO answer : answers) {
                SurveyAnswerUpsertDTO.Item item = new SurveyAnswerUpsertDTO.Item();
                item.setItemId(answer.getItemId());
                item.setScore(answer.getScore());
                item.setOptionLabel(answer.getOptionLabel());
                item.setTextValue(answer.getTextValue());
                items.add(item);
            }
        }
        return surveyService.submitAnswerForPatient(patientId, dispatchId, anonymousFlag, commentText, items);
    }
}
