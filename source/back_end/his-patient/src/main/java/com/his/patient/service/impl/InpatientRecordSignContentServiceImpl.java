package com.his.patient.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.enums.SummaryStatusEnum;
import com.his.common.support.CanonicalText;
import com.his.common.util.NumUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.mapper.BizInpatientRecordMapper;
import com.his.system.service.SignableContentService;
import com.his.spi.vo.SignSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 住院病历文书的签名内容提供者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InpatientRecordSignContentServiceImpl extends ServiceImpl<BizInpatientRecordMapper, BizInpatientRecord> implements SignableContentService {

    private final BizInpatientRecordMapper bizInpatientRecordMapper;

    private static String canonical(BizInpatientRecord r) {
        return CanonicalText.create("INPATIENT_RECORD")
                .put("recordNo", r.getRecordNo())
                .put("admissionId", r.getAdmissionId())
                .put("patientId", r.getPatientId())
                .put("patientNo", r.getPatientNo())
                .put("recordType", r.getRecordType())
                .put("recordTitle", r.getRecordTitle())
                .put("recordTime", r.getRecordTime())
                .put("chiefComplaint", r.getChiefComplaint())
                .put("presentIllness", r.getPresentIllness())
                .put("pastHistory", r.getPastHistory())
                .put("personalHistory", r.getPersonalHistory())
                .put("familyHistory", r.getFamilyHistory())
                .put("allergyHistory", r.getAllergyHistory())
                .put("temperature", NumUtil.plain(r.getTemperature()))
                .put("pulse", r.getPulse())
                .put("respiration", r.getRespiration())
                .put("systolicPressure", r.getSystolicPressure())
                .put("diastolicPressure", r.getDiastolicPressure())
                .put("height", NumUtil.plain(r.getHeight()))
                .put("weight", NumUtil.plain(r.getWeight()))
                .put("generalCondition", r.getGeneralCondition())
                .put("skinMucosa", r.getSkinMucosa())
                .put("headNeck", r.getHeadNeck())
                .put("chestLung", r.getChestLung())
                .put("heart", r.getHeart())
                .put("abdomen", r.getAbdomen())
                .put("spineLimbs", r.getSpineLimbs())
                .put("nervousSystem", r.getNervousSystem())
                .put("specialistExam", r.getSpecialistExam())
                .put("auxiliaryExam", r.getAuxiliaryExam())
                .put("diagnosisName", r.getDiagnosisName())
                .put("diagnosisCode", r.getDiagnosisCode())
                .put("treatmentPlan", r.getTreatmentPlan())
                .put("courseNote", r.getCourseNote())
                .put("doctorId", r.getDoctorId())
                .put("doctorName", r.getDoctorName())
                .build();
    }

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.INPATIENT_RECORD;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizInpatientRecord r = bizInpatientRecordMapper.selectById(bizId);
        if (r == null) {
            return null;
        }
        return new SignSubject(
                r.getId(),
                r.getRecordNo(),
                r.getPatientId(),
                r.getPatientName(),
                r.getDeptId(),
                r.getDeptName(),
                r.getRecordStatus(),
                SummaryStatusEnum.getLabel(r.getRecordStatus()),
                canonical(r));
    }

    @Override
    public String blockReason(SignSubject subject, SignSceneEnum scene) {
        BizInpatientRecord r = bizInpatientRecordMapper.selectById(subject.bizId());
        if (r == null) {
            return "病历文书不存在或已被删除，无法签名";
        }
        if (Objects.equals(1, r.getSignStatus())) {
            return "文书 " + r.getRecordNo() + " 已签名（签名即锁定），不能重复签名；"
                    + "如需修改内容，请先在「签名中心」作废该签名（作废会留痕并解除锁定）";
        }
        if (Objects.equals(RecordStatusEnum.DRAFT.getCode(), r.getRecordStatus())) {
            return "文书 " + r.getRecordNo() + " 还是草稿，不能签名；"
                    + "请先「提交」——提交动作会自动完成签名";
        }
        if (Objects.equals(RecordStatusEnum.ARCHIVED.getCode(), r.getRecordStatus())) {
            return "文书 " + r.getRecordNo() + " 已归档，不支持补签：归档后补签会让签名时刻晚于归档时刻，"
                    + "形成时间线自相矛盾的证据。历史未签名文书必须如实标注为「未签名」";
        }
        return null;
    }

    @Override
    public void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime) {
        BizInpatientRecord r = new BizInpatientRecord();
        r.setId(bizId);
        r.setSignStatus(1);
        r.setSignId(signId);
        r.setSignedTime(signedTime == null ? null : TimeUtil.toSeconds(signedTime));
        bizInpatientRecordMapper.updateById(r);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizInpatientRecord r = bizInpatientRecordMapper.selectById(bizId);
        if (r == null || !Objects.equals(signId, r.getSignId())) {
            // 锚点指向的不是被作废的那条签名：不动它（说明后来又签过一次，不能误伤新签名）
            return;
        }
        // 置「签名已失效」而**保留 sign_id**：从病历能直接跳到那条签名去看"为什么被作废"。
        // 回落成 0-未签名是绝对不行的 —— 那会让"有人作废过签名"这个事实从列表上消失。
        BizInpatientRecord patch = new BizInpatientRecord();
        patch.setId(bizId);
        patch.setSignStatus(2);
        bizInpatientRecordMapper.updateById(patch);
    }

}
