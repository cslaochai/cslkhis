package com.his.emr.service.impl;

import com.his.common.entity.SignSubject;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.emr.service.OutpatientRecordSignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 门诊病历的签名内容提供者（业务类型=2）。
 *
 * <p>与住院病历同一套规则（见 {@code InpatientRecordSignProvider} 的类注释）：
 * 规范化文本只含**病历内容**，绝不含 {@code record_status / review_status / submit_time}
 * 这类会随流程变化的字段 —— 放进去的话，"结诊提交"这一刻签名就当场失效。
 *
 * <p>门诊特殊之处：记录状态有第 4 态「已作废」，已作废的病历不允许签名。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutpatientRecordSignProviderImpl implements SignableContentProvider, OutpatientRecordSignProvider {

    private final BizMedicalRecordMapper recordMapper;

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.OUTPATIENT_RECORD;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizMedicalRecord r = recordMapper.selectById(bizId);
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
        BizMedicalRecord r = recordMapper.selectById(subject.bizId());
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
        patch.setSignedTime(signedTime == null ? null : signedTime.truncatedTo(ChronoUnit.SECONDS));
        recordMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizMedicalRecord r = recordMapper.selectById(bizId);
        if (r == null || !Objects.equals(signId, r.getSignId())) {
            return;
        }
        BizMedicalRecord patch = new BizMedicalRecord();
        patch.setId(bizId);
        patch.setSignStatus(ObjectSignStatusEnum.INVALIDATED.getCode());
        recordMapper.updateById(patch);
    }
}
