package com.his.patient.service.impl;

import com.his.patient.service.InpatientLeaveSignProvider;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.enums.ObjectSignStatus;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.entity.SignSubject;
import com.his.common.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.patient.entity.BizInpatientLeave;
import com.his.patient.enums.LeaveStatusEnum;
import com.his.patient.mapper.BizInpatientLeaveMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 住院请假单的签名内容提供者（业务类型=10）—— 医师批准即签。
 *
 * <p>与病危重通知同一套「签名即锁定」：批准那一刻医师电子签名落板并冻结内容，
 * 要改须先在签名中心作废签名（作废留痕、链继续）。
 *
 * <p><b>规范化文本含患方承诺四要素</b>（确认人姓名/关系/电话/签名时点）：它们是「向谁履行了
 * 告知义务、谁承诺了风险自负」的法定事实，作废签名后补签若察觉承诺人被改动，验签必须断。
 * <b>不含</b>流程字段（leave_status、approve_time、actual 系列、overdue 系列、print 系列、cancel 系列）与签名锚点本身
 * —— 流程一推进就验签必断是老坑（申请内容字段在批准时已冻结，后续动作只动流程列）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InpatientLeaveSignProviderImpl implements SignableContentProvider, InpatientLeaveSignProvider {

    private final BizInpatientLeaveMapper leaveMapper;

    @Override
    public SignBizType bizType() {
        return SignBizType.INPATIENT_LEAVE;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizInpatientLeave l = leaveMapper.selectById(bizId);
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
    public String blockReason(SignSubject subject, SignScene scene) {
        if (scene != SignScene.LEAVE_APPROVE) {
            return "住院请假单只支持「请假审批签名」场景，当前场景「" + scene.getText() + "」不适用";
        }
        BizInpatientLeave l = leaveMapper.selectById(subject.bizId());
        if (l == null) {
            return "请假单不存在或已被删除，无法签名";
        }
        if (Objects.equals(ObjectSignStatus.SIGNED.getCode(), l.getSignStatus())) {
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
    public void applySignAnchor(Long bizId, SignScene scene, Long signId, LocalDateTime signedTime) {
        BizInpatientLeave patch = new BizInpatientLeave();
        patch.setId(bizId);
        patch.setSignStatus(ObjectSignStatus.SIGNED.getCode());
        patch.setSignId(signId);
        patch.setSignedTime(signedTime == null ? null : signedTime.truncatedTo(ChronoUnit.SECONDS));
        leaveMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizInpatientLeave l = leaveMapper.selectById(bizId);
        if (l == null || !Objects.equals(signId, l.getSignId())) {
            return;
        }
        leaveMapper.update(null, new LambdaUpdateWrapper<BizInpatientLeave>()
                .eq(BizInpatientLeave::getId, bizId)
                .set(BizInpatientLeave::getSignStatus, ObjectSignStatus.INVALIDATED.getCode())
                .set(BizInpatientLeave::getSignId, null));
        log.info("住院请假单签名已作废回写 leaveNo={} signId={}", l.getLeaveNo(), signId);
    }
}
