package com.his.emr.service.impl;

import com.his.emr.service.PrescriptionSignProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.enums.PrescriptionStatusEnum;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.entity.SignSubject;
import com.his.common.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.mapper.BizPrescriptionDetailMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * 处方的签名内容提供者（业务类型=4）——**双签**。
 *
 * <p>《处方管理办法》要求处方审核由药师完成，**审方与发药是两道手**：
 * <ul>
 *   <li>{@link SignScene#RX_CREATE} → 写医师签名ID（开方医师）</li>
 *   <li>{@link SignScene#RX_AUDIT}  → 写审核签名ID（审方药师）</li>
 * </ul>
 * 第二环（审方）的签名内容带上第一环的摘要，于是"审方之后又改了处方内容"会同时打断
 * 第二环的验签 —— 否则两次签名各自绑同一份内容，改谁都验得过去，双签就成了摆设。
 *
 * <p><b>规范化里绝不能出现 {@code prescription_status / payment_status / submit_time /
 * audit_time / dispense_time} 这类流程字段</b>：它们会随流程变，
 * 放进去等于"一审方，开方签名当场失效"。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrescriptionSignProviderImpl implements SignableContentProvider, PrescriptionSignProvider {

    private final BizPrescriptionMapper prescriptionMapper;
    private final BizPrescriptionDetailMapper detailMapper;

    @Override
    public SignBizType bizType() {
        return SignBizType.PRESCRIPTION;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizPrescription p = prescriptionMapper.selectById(bizId);
        if (p == null) {
            return null;
        }
        return new SignSubject(
                p.getId(),
                p.getPrescriptionNo(),
                p.getPatientId(),
                p.getPatientName(),
                p.getDeptId(),
                p.getDeptName(),
                p.getPrescriptionStatus(),
                PrescriptionSignProvider.rxStatusText(p.getPrescriptionStatus()),
                canonical(p));
    }

    @Override
    public String blockReason(SignSubject subject, SignScene scene) {
        BizPrescription p = prescriptionMapper.selectById(subject.bizId());
        if (p == null) {
            return "处方不存在或已被删除，无法签名";
        }
        Integer status = p.getPrescriptionStatus();
        if (Objects.equals(PrescriptionStatusEnum.CANCELLED.getCode(), status)
                || Objects.equals(PrescriptionStatusEnum.RETURNED.getCode(), status)) {
            return "处方 " + p.getPrescriptionNo() + " 已" + PrescriptionSignProvider.rxStatusText(status) + "，不能签名";
        }
        if (scene == SignScene.RX_CREATE) {
            if (p.getDoctorSignId() != null) {
                return "处方 " + p.getPrescriptionNo() + " 已有开方签名，不能重复签名；"
                        + "如需修改，请先在「签名中心」作废该签名";
            }
            return null;
        }
        if (scene == SignScene.RX_AUDIT) {
            if (p.getAuditSignId() != null) {
                return "处方 " + p.getPrescriptionNo() + " 已有审方签名，不能重复签名；"
                        + "如需修改，请先在「签名中心」作废该签名";
            }
            // 本期没有独立的「处方提交」动作（医生开方落库即 prescription_status=1），
            // 所以 1-草稿与 2-已提交都视为"未审、可审"；已审核/已发药/已取消/已退药一律拒绝。
            if (!Objects.equals(PrescriptionStatusEnum.DRAFT.getCode(), status)
                    && !Objects.equals(PrescriptionStatusEnum.SUBMITTED.getCode(), status)) {
                return "处方 " + p.getPrescriptionNo() + " 当前是「" + PrescriptionSignProvider.rxStatusText(status)
                        + "」，不能审方；只有「草稿」或「已提交」的处方才能审核";
            }
            return null;
        }
        return "处方只支持「开方签名」与「审方签名」两种场景，当前场景「" + scene.getText() + "」不适用";
    }

    @Override
    public void applySignAnchor(Long bizId, SignScene scene, Long signId, LocalDateTime signedTime) {
        BizPrescription patch = new BizPrescription();
        patch.setId(bizId);
        LocalDateTime t = signedTime == null ? null : signedTime.truncatedTo(ChronoUnit.SECONDS);
        if (scene == SignScene.RX_AUDIT) {
            patch.setAuditSignId(signId);
            patch.setAuditSignedTime(t);
        } else {
            patch.setDoctorSignId(signId);
            patch.setDoctorSignedTime(t);
        }
        prescriptionMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizPrescription p = prescriptionMapper.selectById(bizId);
        if (p == null) {
            return;
        }
        // 处方行没有 sign_status 列，表达"没有有效签名"只能清指针。
        // MyBatis-Plus 的 updateById 只更新非 null 字段（set null 等于"不改"），
        // 所以必须用 UpdateWrapper 显式 set null，否则签名作废了、处方上还挂着旧签名ID。
        if (Objects.equals(signId, p.getDoctorSignId())) {
            prescriptionMapper.update(null, new LambdaUpdateWrapper<BizPrescription>()
                    .eq(BizPrescription::getId, bizId)
                    .set(BizPrescription::getDoctorSignId, null)
                    .set(BizPrescription::getDoctorSignedTime, null));
            log.info("已清除处方开方签名指针 rxNo={} signId={}", p.getPrescriptionNo(), signId);
        }
        if (Objects.equals(signId, p.getAuditSignId())) {
            prescriptionMapper.update(null, new LambdaUpdateWrapper<BizPrescription>()
                    .eq(BizPrescription::getId, bizId)
                    .set(BizPrescription::getAuditSignId, null)
                    .set(BizPrescription::getAuditSignedTime, null));
            log.info("已清除处方审方签名指针 rxNo={} signId={}", p.getPrescriptionNo(), signId);
        }
    }

    /**
     * 规范化文本：只含"处方内容"（处方头 + 明细），**不含任何流程字段**。
     *
     * <p>明细按 {@code id} 升序拼装 —— 顺序必须确定，否则同一张处方两次签名摘要不同。
     */
    public String canonical(BizPrescription p) {
        List<BizPrescriptionDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<BizPrescriptionDetail>()
                        .eq(BizPrescriptionDetail::getPrescriptionId, p.getId())
                        .orderByAsc(BizPrescriptionDetail::getId));
        return CanonicalText.create("PRESCRIPTION")
                .put("prescriptionNo", p.getPrescriptionNo())
                .put("patientId", p.getPatientId())
                .put("patientNo", p.getPatientNo())
                .put("visitDate", p.getVisitDate())
                .put("registId", p.getRegistId())
                .put("recordId", p.getRecordId())
                .put("recordNo", p.getRecordNo())
                .put("deptId", p.getDeptId())
                .put("deptName", p.getDeptName())
                .put("doctorId", p.getDoctorId())
                .put("doctorName", p.getDoctorName())
                .put("prescriptionType", p.getPrescriptionType())
                .put("prescriptionSource", p.getPrescriptionSource())
                .put("diagnosis", p.getDiagnosis())
                .put("totalAmount", plain(p.getTotalAmount()))
                .put("drugCount", p.getDrugCount())
                .put("usageInstruction", p.getUsageInstruction())
                .put("isUrgent", p.getIsUrgent())
                .put("details", detailsText(details))
                .build();
    }

    /** 明细逐条一行，字段用 {@code |} 分隔、行间换行 —— 全部为"用药内容"，不含明细行的流程状态 */
    private static String detailsText(List<BizPrescriptionDetail> details) {
        if (details == null || details.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (BizPrescriptionDetail d : details) {
            i++;
            sb.append(i).append('|')
                    .append(nz(d.getDrugCode())).append('|')
                    .append(nz(d.getDrugName())).append('|')
                    .append(nz(d.getGenericName())).append('|')
                    .append(nz(d.getSpecification())).append('|')
                    .append(nz(d.getDosageForm())).append('|')
                    .append(nz(d.getUnit())).append('|')
                    .append(plain(d.getQuantity())).append('|')
                    .append(plain(d.getPrice())).append('|')
                    .append(plain(d.getAmount())).append('|')
                    .append(nz(d.getSingleDosage())).append('|')
                    .append(nz(d.getUsageDosage())).append('|')
                    .append(nz(d.getFrequency())).append('|')
                    .append(nz(d.getRoute())).append('|')
                    .append(d.getDuration() == null ? "" : d.getDuration()).append('|')
                    .append(d.getIsSkinTest() == null ? "" : d.getIsSkinTest())
                    .append('\n');
        }
        return sb.toString();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /** BigDecimal 去尾零：金额 `10.00` 与 `10.0` 必须算出同一个摘要 */
    private static String plain(BigDecimal v) {
        return v == null ? null : v.stripTrailingZeros().toPlainString();
    }
}
