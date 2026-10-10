package com.his.emr.service.impl;

import com.his.system.entity.SignSubject;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.system.service.SignableContentProvider;
import com.his.common.util.TimeUtil;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.emr.service.OutpatientRecordSignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 门诊病历的签名内容提供者（业务类型=2）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutpatientRecordSignProviderImpl implements SignableContentProvider, OutpatientRecordSignProvider {

    private final BizMedicalRecordMapper bizMedicalRecordMapper;

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.OUTPATIENT_RECORD;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizMedicalRecord r = bizMedicalRecordMapper.selectById(bizId);
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
                OutpatientRecordSignProvider.recordStatusText(r.getRecordStatus()),
                OutpatientRecordSignProvider.canonical(r));
    }

    @Override
    public String blockReason(SignSubject subject, SignSceneEnum scene) {
        BizMedicalRecord r = bizMedicalRecordMapper.selectById(subject.bizId());
        if (r == null) {
            return "门诊病历不存在或已被删除，无法签名";
        }
        if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), r.getSignStatus())) {
            return "门诊病历 " + r.getRecordNo() + " 已签名（签名即锁定），不能重复签名；"
                    + "如需修改，请先在「签名中心」作废该签名（作废会留痕并解除锁定）";
        }
        Integer status = r.getRecordStatus();
        if (Objects.equals(RecordStatusEnum.DRAFT.getCode(), status)) {
            return "门诊病历 " + r.getRecordNo() + " 还是草稿，不能签名；请先完成「结诊提交」——"
                    + "提交动作会自动完成签名";
        }
        if (Objects.equals(RecordStatusEnum.VOIDED.getCode(), status)) {
            return "门诊病历 " + r.getRecordNo() + " 已作废，不能签名";
        }
        return null;
    }

    @Override
    public void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime) {
        BizMedicalRecord patch = new BizMedicalRecord();
        patch.setId(bizId);
        patch.setSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
        patch.setSignId(signId);
        patch.setSignedTime(signedTime == null ? null : TimeUtil.toSeconds(signedTime));
        bizMedicalRecordMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizMedicalRecord r = bizMedicalRecordMapper.selectById(bizId);
        if (r == null || !Objects.equals(signId, r.getSignId())) {
            return;
        }
        BizMedicalRecord patch = new BizMedicalRecord();
        patch.setId(bizId);
        patch.setSignStatus(ObjectSignStatusEnum.INVALIDATED.getCode());
        bizMedicalRecordMapper.updateById(patch);
    }
}
