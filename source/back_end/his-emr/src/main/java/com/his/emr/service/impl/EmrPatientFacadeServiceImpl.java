package com.his.emr.service.impl;

import com.his.emr.dto.PrevisitSubmitDTO;
import com.his.emr.service.EmrPatientFacadeService;
import com.his.emr.vo.BizFollowupTaskVO;
import com.his.emr.vo.ChronicMyRecordsVO;
import com.his.emr.vo.MyMedicalRecordVO;
import com.his.emr.vo.MyPrescriptionVO;
import com.his.emr.vo.PrevisitDetailVO;
import com.his.emr.vo.PrevisitQuestionnaireVO;
import com.his.emr.service.ChronicRecordService;
import com.his.emr.service.EmrService;
import com.his.emr.service.FollowupTaskService;
import com.his.emr.service.PrescriptionService;
import com.his.emr.service.PrevisitRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 患者端 EMR 读取端口实现（his-emr）：委托既有服务，对外只暴露 his-api 契约。
 */
@Service
@RequiredArgsConstructor
public class EmrPatientFacadeServiceImpl implements EmrPatientFacadeService {

    private final EmrService emrService;
    private final PrescriptionService prescriptionService;
    private final FollowupTaskService followupTaskService;
    private final ChronicRecordService chronicRecordService;
    private final PrevisitRecordService previsitRecordService;

    @Override
    public List<MyMedicalRecordVO> myRecords(Long patientId) {
        return emrService.myRecords(patientId);
    }

    @Override
    public MyMedicalRecordVO myRecordDetail(Long patientId, Long recordId) {
        return emrService.myRecordDetail(patientId, recordId);
    }

    @Override
    public List<MyPrescriptionVO> myPrescriptions(Long patientId) {
        return prescriptionService.myPrescriptions(patientId);
    }

    @Override
    public List<BizFollowupTaskVO> listForPatient(Long patientId) {
        return followupTaskService.listForPatient(patientId);
    }

    @Override
    public boolean replyFromPatient(Long taskId, Long patientId, String replyText) {
        return followupTaskService.replyFromPatient(taskId, patientId, replyText);
    }

    @Override
    public ChronicMyRecordsVO myRecords() {
        return chronicRecordService.myRecords();
    }

    @Override
    public PrevisitQuestionnaireVO questionnaire() {
        return previsitRecordService.questionnaire();
    }

    @Override
    public PrevisitDetailVO submit(PrevisitSubmitDTO dto) {
        return previsitRecordService.submit(dto);
    }

    @Override
    public PrevisitDetailVO getByRegist(Long registId) {
        return previsitRecordService.getByRegist(registId);
    }
}
