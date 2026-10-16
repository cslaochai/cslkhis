package com.his.api.service;

import com.his.api.dto.PrevisitSubmitDTO;
import com.his.api.vo.BizFollowupTaskVO;
import com.his.api.vo.ChronicMyRecordsVO;
import com.his.api.vo.MyMedicalRecordVO;
import com.his.api.vo.MyPrescriptionVO;
import com.his.api.vo.PrevisitDetailVO;
import com.his.api.vo.PrevisitQuestionnaireVO;

import java.util.List;

/**
 * 患者端 EMR 读取端口（his-emr 实现）。
 *
 * <p>miniapp 只依赖本端口，不直接依赖 his-emr 的 {@code EmrService} / {@code PrescriptionService} /
 * {@code FollowupTaskService} / {@code ChronicRecordService} / {@code PrevisitRecordService}。
 */
public interface EmrPatientApi {

    /**
     * 患者端·我的病历列表（只给已提交/已归档）。
     */
    List<MyMedicalRecordVO> myRecords(Long patientId);

    /**
     * 患者端·病历详情（recordId 必须属于 patientId，否则按不存在处理）。
     */
    MyMedicalRecordVO myRecordDetail(Long patientId, Long recordId);

    /**
     * 患者端·我的处方（按就诊人查全部处方，不含草稿）。
     */
    List<MyPrescriptionVO> myPrescriptions(Long patientId);

    /**
     * 患者端「我的随访」。
     */
    List<BizFollowupTaskVO> listForPatient(Long patientId);

    /**
     * 患者提交随访反馈（任务必须属于该患者、未被取消才允许）。
     */
    boolean replyFromPatient(Long taskId, Long patientId, String replyText);

    /**
     * 患者端「我的慢病档案」（patientId 取自登录态）。
     */
    ChronicMyRecordsVO myRecords();

    /**
     * 预问诊量表（题目结构随接口下发）。
     */
    PrevisitQuestionnaireVO questionnaire();

    /**
     * 提交预问诊问卷（按挂号 upsert）。
     */
    PrevisitDetailVO submit(PrevisitSubmitDTO dto);

    /**
     * 按挂号取预问诊记录（无则 null）。
     */
    PrevisitDetailVO getByRegist(Long registId);
}
