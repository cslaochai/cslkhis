package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.entity.SignSubject;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.common.util.TimeUtil;
import com.his.patient.entity.BizInpatientLeave;
import com.his.patient.enums.LeaveStatusEnum;
import com.his.patient.mapper.BizInpatientLeaveMapper;
import com.his.patient.service.InpatientLeaveSignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 住院请假单的签名内容提供者（业务类型=10）—— 医师批准即签。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InpatientLeaveSignProviderImpl implements SignableContentProvider, InpatientLeaveSignProvider {

    private final BizInpatientLeaveMapper bizInpatientLeaveMapper;

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.INPATIENT_LEAVE;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizInpatientLeave l = bizInpatientLeaveMapper.selectById(bizId);
        if (l == null) {
            return null;
        }
        return new SignSubject(
                l.getId(),
                l.getLeaveNo(),
                l.getPatientId(),
                l.getPatientName(),
                l.getDeptId(),
                l.getDeptName(),
                l.getLeaveStatus(),
                BizInpatientLeave.statusText(l.getLeaveStatus()),
                InpatientLeaveSignProvider.canonical(l));
    }

    @Override
    public String blockReason(SignSubject subject, SignSceneEnum scene) {
        if (scene != SignSceneEnum.LEAVE_APPROVE) {
            return "住院请假单只支持「请假审批签名」场景，当前场景「" + scene.getText() + "」不适用";
        }
        BizInpatientLeave l = bizInpatientLeaveMapper.selectById(subject.bizId());
        if (l == null) {
            return "请假单不存在或已被删除，无法签名";
        }
        if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), l.getSignStatus())) {
            return "请假单 " + l.getLeaveNo() + " 已签名（签名即锁定），不能重复签名；"
                    + "如需修改，请先在「签名中心」作废该签名（作废会留痕并解除锁定）";
        }
        if (!Objects.equals(LeaveStatusEnum.PENDING.getCode(), l.getLeaveStatus())) {
            return "请假单 " + l.getLeaveNo() + " 当前为「" + BizInpatientLeave.statusText(l.getLeaveStatus())
                    + "」，只有「待审批」的单在批准时签名";
        }
        return null;
    }

    @Override
    public void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime) {
        BizInpatientLeave patch = new BizInpatientLeave();
        patch.setId(bizId);
        patch.setSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
        patch.setSignId(signId);
        patch.setSignedTime(signedTime == null ? null : TimeUtil.toSeconds(signedTime));
        bizInpatientLeaveMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizInpatientLeave l = bizInpatientLeaveMapper.selectById(bizId);
        if (l == null || !Objects.equals(signId, l.getSignId())) {
            return;
        }
        bizInpatientLeaveMapper.update(null, new LambdaUpdateWrapper<BizInpatientLeave>()
                .eq(BizInpatientLeave::getId, bizId)
                .set(BizInpatientLeave::getSignStatus, ObjectSignStatusEnum.INVALIDATED.getCode())
                .set(BizInpatientLeave::getSignId, null));
        log.info("住院请假单签名已作废回写 leaveNo={} signId={}", l.getLeaveNo(), signId);
    }
}
