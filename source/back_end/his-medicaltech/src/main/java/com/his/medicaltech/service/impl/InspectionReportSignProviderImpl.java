package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.entity.SignSubject;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.common.util.TimeUtil;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.enums.InsRecordStatusEnum;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.medicaltech.service.InspectionReportSignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 检查报告的签名内容提供者（业务类型=5）——**双签**。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionReportSignProviderImpl implements SignableContentProvider, InspectionReportSignProvider {

    private final BizInspectionRecordMapper bizInspectionRecordMapper;

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.INSPECTION_REPORT;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizInspectionRecord r = bizInspectionRecordMapper.selectById(bizId);
        if (r == null) {
            return null;
        }
        return new SignSubject(
                r.getId(),
                r.getRecordNo(),
                r.getPatientId(),
                r.getPatientName(),
                r.getInspectionDeptId(),
                r.getInspectionDeptName(),
                r.getRecordStatus(),
                InspectionReportSignProvider.statusText(r.getRecordStatus()),
                InspectionReportSignProvider.canonical(r));
    }

    @Override
    public String blockReason(SignSubject subject, SignSceneEnum scene) {
        BizInspectionRecord r = bizInspectionRecordMapper.selectById(subject.bizId());
        if (r == null) {
            return "检查记录不存在或已被删除，无法签名";
        }
        Integer status = r.getRecordStatus();
        if (Objects.equals(InsRecordStatusEnum.CANCELLED.getCode(), status)) {
            return "检查记录 " + r.getRecordNo() + " 已取消，不能签名";
        }
        boolean resultReady = Objects.equals(InsRecordStatusEnum.RESULTED.getCode(), status)
                || Objects.equals(InsRecordStatusEnum.REVIEWED.getCode(), status)
                || Objects.equals(InsRecordStatusEnum.PUBLISHED.getCode(), status);

        if (scene == SignSceneEnum.REPORT_ISSUE) {
            if (r.getReportSignId() != null) {
                return "检查报告 " + r.getRecordNo() + " 已有报告医师签名，不能重复签名；"
                        + "如需修改，请先在「签名中心」作废该签名";
            }
            if (!resultReady) {
                return "检查记录 " + r.getRecordNo() + " 当前是「" + InspectionReportSignProvider.statusText(status)
                        + "」，还没有出结果，不能签报告名；请先完成「出结果」";
            }
            return null;
        }
        if (scene == SignSceneEnum.REPORT_AUDIT) {
            if (r.getAuditSignId() != null) {
                return "检查报告 " + r.getRecordNo() + " 已有审核医师签名，不能重复签名；"
                        + "如需修改，请先在「签名中心」作废该签名";
            }
            if (!resultReady) {
                return "检查记录 " + r.getRecordNo() + " 当前是「" + InspectionReportSignProvider.statusText(status)
                        + "」，还没有出结果，不能审核；报告未出结果时审核等于给一份空报告签发";
            }
            return null;
        }
        return "检查报告只支持「报告签名」与「报告审核签名」两种场景，当前场景「"
                + scene.getText() + "」不适用";
    }

    @Override
    public void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime) {
        BizInspectionRecord patch = new BizInspectionRecord();
        patch.setId(bizId);
        LocalDateTime t = signedTime == null ? null : TimeUtil.toSeconds(signedTime);
        if (scene == SignSceneEnum.REPORT_AUDIT) {
            patch.setAuditSignId(signId);
            patch.setAuditSignedTime(t);
        } else {
            patch.setReportSignId(signId);
            patch.setReportSignedTime(t);
        }
        bizInspectionRecordMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizInspectionRecord r = bizInspectionRecordMapper.selectById(bizId);
        if (r == null) {
            return;
        }
        // 表里没有 sign_status 列，表达"没有有效签名"只能清指针；
        // updateById 不更新 null 字段，所以 set null 必须用 UpdateWrapper。
        if (Objects.equals(signId, r.getReportSignId())) {
            bizInspectionRecordMapper.update(null, new LambdaUpdateWrapper<BizInspectionRecord>()
                    .eq(BizInspectionRecord::getId, bizId)
                    .set(BizInspectionRecord::getReportSignId, null)
                    .set(BizInspectionRecord::getReportSignedTime, null));
            log.info("已清除检查报告签名指针 recordNo={} signId={}", r.getRecordNo(), signId);
        }
        if (Objects.equals(signId, r.getAuditSignId())) {
            bizInspectionRecordMapper.update(null, new LambdaUpdateWrapper<BizInspectionRecord>()
                    .eq(BizInspectionRecord::getId, bizId)
                    .set(BizInspectionRecord::getAuditSignId, null)
                    .set(BizInspectionRecord::getAuditSignedTime, null));
            log.info("已清除检查报告审核签名指针 recordNo={} signId={}", r.getRecordNo(), signId);
        }
    }

}
