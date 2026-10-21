package com.his.clinical.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.ApplyStatusEnum;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.support.CanonicalText;
import com.his.common.util.NumUtil;
import com.his.common.util.TimeUtil;
import com.his.clinical.entity.BizInspectionApply;
import com.his.clinical.mapper.BizInspectionApplyMapper;
import com.his.spi.service.SignableContentService;
import com.his.spi.vo.SignSubject;
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
public class InspectionApplySignContentServiceImpl extends ServiceImpl<BizInspectionApplyMapper, BizInspectionApply> implements SignableContentService {

    private final BizInspectionApplyMapper bizInspectionApplyMapper;

    private static String statusText(Integer status) {
        return status == null ? "—" : ApplyStatusEnum.getLabelOrUnknown(status);
    }

    private static String canonical(BizInspectionApply a) {
        return CanonicalText.create("INSPECTION_APPLY")
                .put("applyNo", a.getApplyNo())
                .put("patientId", a.getPatientId())
                .put("patientNo", a.getPatientNo())
                .put("patientName", a.getPatientName())
                .put("gender", a.getGender())
                .put("age", a.getAge())
                .put("registId", a.getRegistId())
                .put("visitDate", a.getVisitDate())
                .put("deptId", a.getDeptId())
                .put("deptName", a.getDeptName())
                .put("doctorId", a.getDoctorId())
                .put("doctorName", a.getDoctorName())
                .put("inspectionItemId", a.getInspectionItemId())
                .put("inspectionItemCode", a.getInspectionItemCode())
                .put("inspectionItemName", a.getInspectionItemName())
                .put("inspectionDeptId", a.getInspectionDeptId())
                .put("inspectionDeptName", a.getInspectionDeptName())
                .put("bodyPart", a.getBodyPart())
                .put("inspectionPurpose", a.getInspectionPurpose())
                .put("clinicalDiagnosis", a.getClinicalDiagnosis())
                .put("diseaseSummary", a.getDiseaseSummary())
                .put("specialRequirements", a.getSpecialRequirements())
                .put("isEmergency", a.getIsEmergency())
                .put("price", NumUtil.plain(a.getPrice()))
                .build();
    }

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
                statusText(a.getApplyStatus()),
                canonical(a));
    }

    @Override
    public String blockReason(SignSubject subject, SignSceneEnum scene) {
        if (scene != SignSceneEnum.APPLY_CREATE) {
            return "检查申请单只支持「申请开立签名」场景，当前场景「" + scene.getLabel() + "」不适用";
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
