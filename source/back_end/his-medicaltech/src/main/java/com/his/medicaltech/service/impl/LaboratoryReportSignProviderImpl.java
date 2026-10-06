package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.entity.SignSubject;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.enums.LabRecordStatusEnum;
import com.his.medicaltech.mapper.BizLabResultMapper;
import com.his.medicaltech.mapper.BizLaboratoryRecordMapper;
import com.his.medicaltech.service.LaboratoryReportSignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * 检验报告的签名内容提供者（业务类型=6）——**双签**（与检查报告同构）。
 *
 * <p>与检查报告的唯一区别：**被签内容必须包含检验结果明细**（检验结果）。
 * 检验报告的实质内容就是那一串"项目 + 结果值 + 单位 + 参考范围 + 异常标志"，
 * 只签一个 record 头等于签了个空信封 —— 把"白细胞 1.0"改成"白细胞 10.0"照样验得过去。
 *
 * <p>明细按 {@code sort_order, id} 升序拼装，顺序必须确定。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LaboratoryReportSignProviderImpl implements SignableContentProvider, LaboratoryReportSignProvider {

    private final BizLaboratoryRecordMapper recordMapper;
    private final BizLabResultMapper labResultMapper;

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.LAB_REPORT;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizLaboratoryRecord r = recordMapper.selectById(bizId);
        if (r == null) {
            return null;
        }
        return new SignSubject(
                r.getId(),
                r.getRecordNo(),
                r.getPatientId(),
                r.getPatientName(),
                r.getLaboratoryDeptId(),
                r.getLaboratoryDeptName(),
                r.getRecordStatus(),
                LaboratoryReportSignProvider.statusText(r.getRecordStatus()),
                LaboratoryReportSignProvider.canonical(r, resultsOf(r.getId())));
    }

    @Override
    public String blockReason(SignSubject subject, SignSceneEnum scene) {
        BizLaboratoryRecord r = recordMapper.selectById(subject.bizId());
        if (r == null) {
            return "检验记录不存在或已被删除，无法签名";
        }
        Integer status = r.getRecordStatus();
        if (Objects.equals(LabRecordStatusEnum.CANCELLED.getCode(), status)) {
            return "检验记录 " + r.getRecordNo() + " 已取消，不能签名";
        }
        boolean resultReady = Objects.equals(LabRecordStatusEnum.RESULTED.getCode(), status)
                || Objects.equals(LabRecordStatusEnum.REVIEWED.getCode(), status)
                || Objects.equals(LabRecordStatusEnum.RELEASED.getCode(), status);

        if (scene == SignSceneEnum.REPORT_ISSUE) {
            if (r.getReportSignId() != null) {
                return "检验报告 " + r.getRecordNo() + " 已有报告医师签名，不能重复签名；"
                        + "如需修改，请先在「签名中心」作废该签名";
            }
            if (!resultReady) {
                return "检验记录 " + r.getRecordNo() + " 当前是「" + LaboratoryReportSignProvider.statusText(status)
                        + "」，还没有出结果，不能签报告名；请先完成「录入结果」";
            }
            return null;
        }
        if (scene == SignSceneEnum.REPORT_AUDIT) {
            if (r.getAuditSignId() != null) {
                return "检验报告 " + r.getRecordNo() + " 已有审核医师签名，不能重复签名；"
                        + "如需修改，请先在「签名中心」作废该签名";
            }
            if (!resultReady) {
                return "检验记录 " + r.getRecordNo() + " 当前是「" + LaboratoryReportSignProvider.statusText(status)
                        + "」，还没有出结果，不能审核；报告未出结果时审核等于给一份空报告签发";
            }
            return null;
        }
        return "检验报告只支持「报告签名」与「报告审核签名」两种场景，当前场景「"
                + scene.getText() + "」不适用";
    }

    @Override
    public void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime) {
        BizLaboratoryRecord patch = new BizLaboratoryRecord();
        patch.setId(bizId);
        LocalDateTime t = signedTime == null ? null : signedTime.truncatedTo(ChronoUnit.SECONDS);
        if (scene == SignSceneEnum.REPORT_AUDIT) {
            patch.setAuditSignId(signId);
            patch.setAuditSignedTime(t);
        } else {
            patch.setReportSignId(signId);
            patch.setReportSignedTime(t);
        }
        recordMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizLaboratoryRecord r = recordMapper.selectById(bizId);
        if (r == null) {
            return;
        }
        if (Objects.equals(signId, r.getReportSignId())) {
            recordMapper.update(null, new LambdaUpdateWrapper<BizLaboratoryRecord>()
                    .eq(BizLaboratoryRecord::getId, bizId)
                    .set(BizLaboratoryRecord::getReportSignId, null)
                    .set(BizLaboratoryRecord::getReportSignedTime, null));
            log.info("已清除检验报告签名指针 recordNo={} signId={}", r.getRecordNo(), signId);
        }
        if (Objects.equals(signId, r.getAuditSignId())) {
            recordMapper.update(null, new LambdaUpdateWrapper<BizLaboratoryRecord>()
                    .eq(BizLaboratoryRecord::getId, bizId)
                    .set(BizLaboratoryRecord::getAuditSignId, null)
                    .set(BizLaboratoryRecord::getAuditSignedTime, null));
            log.info("已清除检验报告审核签名指针 recordNo={} signId={}", r.getRecordNo(), signId);
        }
    }

    /**
     * 结果明细（按 sort_order, id 升序 —— 顺序必须确定）
     */
    private List<BizLabResult> resultsOf(Long recordId) {
        return labResultMapper.selectList(new LambdaQueryWrapper<BizLabResult>()
                .eq(BizLabResult::getRecordId, recordId)
                .orderByAsc(BizLabResult::getSortOrder)
                .orderByAsc(BizLabResult::getId));
    }

}
