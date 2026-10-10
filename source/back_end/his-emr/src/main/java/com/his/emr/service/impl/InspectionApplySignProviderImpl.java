package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.system.entity.SignSubject;
import com.his.common.enums.ApplyStatusEnum;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.system.service.SignableContentProvider;
import com.his.common.util.TimeUtil;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.mapper.BizInspectionApplyMapper;
import com.his.emr.service.InspectionApplySignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 检查申请单的签名内容提供者（业务类型=7）—— 开单医师签名。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionApplySignProviderImpl implements SignableContentProvider, InspectionApplySignProvider {

    private final BizInspectionApplyMapper bizInspectionApplyMapper;

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.INSPECTION_APPLY;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizInspectionApply a = bizInspectionApplyMapper.selectById(bizId);
        if (a == null) {
            return null;
        }
        return new SignSubject(
                a.getId(),
                a.getApplyNo(),
                a.getPatientId(),
                a.getPatientName(),
                a.getDeptId(),
                a.getDeptName(),
                a.getApplyStatus(),
                InspectionApplySignProvider.statusText(a.getApplyStatus()),
                InspectionApplySignProvider.canonical(a));
    }

    @Override
    public String blockReason(SignSubject subject, SignSceneEnum scene) {
        if (scene != SignSceneEnum.APPLY_CREATE) {
            return "检查申请单只支持「申请开立签名」场景，当前场景「" + scene.getText() + "」不适用";
        }
        BizInspectionApply a = bizInspectionApplyMapper.selectById(subject.bizId());
        if (a == null) {
            return "检查申请单不存在或已被删除，无法签名";
        }
        if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), a.getSignStatus())) {
            return "检查申请单 " + a.getApplyNo() + " 已签名（签名即锁定），不能重复签名；"
                    + "如需修改，请先在「签名中心」作废该签名（作废会留痕并解除锁定）";
        }
        if (Objects.equals(ApplyStatusEnum.CANCELLED.getCode(), a.getApplyStatus())) {
            return "检查申请单 " + a.getApplyNo() + " 已取消，不能签名";
        }
        return null;
    }

    @Override
    public void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime) {
        BizInspectionApply patch = new BizInspectionApply();
        patch.setId(bizId);
        patch.setSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
        patch.setSignId(signId);
        patch.setSignedTime(signedTime == null ? null : TimeUtil.toSeconds(signedTime));
        bizInspectionApplyMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizInspectionApply a = bizInspectionApplyMapper.selectById(bizId);
        if (a == null || !Objects.equals(signId, a.getSignId())) {
            return;
        }
        bizInspectionApplyMapper.update(null, new LambdaUpdateWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getId, bizId)
                .set(BizInspectionApply::getSignStatus, ObjectSignStatusEnum.INVALIDATED.getCode())
                .set(BizInspectionApply::getSignId, null));
        log.info("检查申请单签名已作废回写 applyNo={} signId={}", a.getApplyNo(), signId);
    }
}
