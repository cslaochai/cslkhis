package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.entity.SignSubject;
import com.his.common.enums.ApplyStatusEnum;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.common.util.TimeUtil;
import com.his.emr.entity.BizLaboratoryApply;
import com.his.emr.mapper.BizLaboratoryApplyMapper;
import com.his.emr.service.LaboratoryApplySignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 检验申请单的签名内容提供者（业务类型=8）—— 开单医师签名。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LaboratoryApplySignProviderImpl implements SignableContentProvider, LaboratoryApplySignProvider {

    private final BizLaboratoryApplyMapper bizLaboratoryApplyMapper;

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.LAB_APPLY;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizLaboratoryApply a = bizLaboratoryApplyMapper.selectById(bizId);
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
                LaboratoryApplySignProvider.statusText(a.getApplyStatus()),
                LaboratoryApplySignProvider.canonical(a));
    }

    @Override
    public String blockReason(SignSubject subject, SignSceneEnum scene) {
        if (scene != SignSceneEnum.APPLY_CREATE) {
            return "检验申请单只支持「申请开立签名」场景，当前场景「" + scene.getText() + "」不适用";
        }
        BizLaboratoryApply a = bizLaboratoryApplyMapper.selectById(subject.bizId());
        if (a == null) {
            return "检验申请单不存在或已被删除，无法签名";
        }
        if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), a.getSignStatus())) {
            return "检验申请单 " + a.getApplyNo() + " 已签名（签名即锁定），不能重复签名；"
                    + "如需修改，请先在「签名中心」作废该签名（作废会留痕并解除锁定）";
        }
        if (Objects.equals(ApplyStatusEnum.CANCELLED.getCode(), a.getApplyStatus())) {
            return "检验申请单 " + a.getApplyNo() + " 已取消，不能签名";
        }
        return null;
    }

    @Override
    public void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime) {
        BizLaboratoryApply patch = new BizLaboratoryApply();
        patch.setId(bizId);
        patch.setSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
        patch.setSignId(signId);
        patch.setSignedTime(signedTime == null ? null : TimeUtil.toSeconds(signedTime));
        bizLaboratoryApplyMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizLaboratoryApply a = bizLaboratoryApplyMapper.selectById(bizId);
        if (a == null || !Objects.equals(signId, a.getSignId())) {
            return;
        }
        bizLaboratoryApplyMapper.update(null, new LambdaUpdateWrapper<BizLaboratoryApply>()
                .eq(BizLaboratoryApply::getId, bizId)
                .set(BizLaboratoryApply::getSignStatus, ObjectSignStatusEnum.INVALIDATED.getCode())
                .set(BizLaboratoryApply::getSignId, null));
        log.info("检验申请单签名已作废回写 applyNo={} signId={}", a.getApplyNo(), signId);
    }
}
