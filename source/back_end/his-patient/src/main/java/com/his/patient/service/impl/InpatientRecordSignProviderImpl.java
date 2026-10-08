package com.his.patient.service.impl;

import com.his.common.entity.SignSubject;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.common.util.TimeUtil;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.enums.SummaryStatusEnum;
import com.his.patient.mapper.BizInpatientRecordMapper;
import com.his.patient.service.InpatientRecordSignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 住院病历文书的签名内容提供者（业务类型=1）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InpatientRecordSignProviderImpl implements SignableContentProvider, InpatientRecordSignProvider {

    private final BizInpatientRecordMapper bizInpatientRecordMapper;

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
                SummaryStatusEnum.getText(r.getRecordStatus()),
                InpatientRecordSignProvider.canonical(r));
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
