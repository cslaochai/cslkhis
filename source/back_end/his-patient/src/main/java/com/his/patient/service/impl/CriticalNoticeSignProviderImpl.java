package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.entity.SignSubject;
import com.his.common.enums.ObjectSignStatus;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.service.SignableContentProvider;
import com.his.patient.entity.BizCriticalNotice;
import com.his.patient.enums.NoticeStatusEnum;
import com.his.patient.mapper.BizCriticalNoticeMapper;
import com.his.patient.service.CriticalNoticeSignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 病危重通知的签名内容提供者（业务类型=9）—— 告知医师签发即签。
 *
 * <p>与申请单同一套「签名即锁定」：签发那一刻医师电子签名落板并冻结内容，
 * 要改须先在签名中心作废签名（作废留痕、链继续）。
 *
 * <p><b>规范化文本不含签收人四要素</b>（姓名/关系/证件/电话）：医师签名发生在「告知」当时，
 * 家属常在数小时后才到院补签，把后写字段放进摘要等于「每次正常签收都验签必断」——
 * 这是签名时序铁律（摘要只覆盖签名当时已存在的内容）。这四要素的防改另有去处：
 * 签收后单据进入 3-已签收，<b>没有任何写路径</b>能再改它们（upsert 只放行草稿、
 * acknowledge 只放行已签发），且手写签名图与原证件号一同留档，改无可改。
 * <b>同样不含</b>流程字段（notice_status/issue_time/acknowledge_time/print_*）与签名锚点本身
 * —— 流程一推进就验签必断是老坑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriticalNoticeSignProviderImpl implements SignableContentProvider, CriticalNoticeSignProvider {

    private final BizCriticalNoticeMapper noticeMapper;

    @Override
    public SignBizType bizType() {
        return SignBizType.CRITICAL_NOTICE;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizCriticalNotice n = noticeMapper.selectById(bizId);
        if (n == null) {
            return null;
        }
        return new SignSubject(
                n.getId(),
                n.getNoticeNo(),
                n.getPatientId(),
                n.getPatientName(),
                n.getDeptId(),
                n.getDeptName(),
                n.getNoticeStatus(),
                BizCriticalNotice.statusText(n.getNoticeStatus()),
                CriticalNoticeSignProvider.canonical(n));
    }

    @Override
    public String blockReason(SignSubject subject, SignScene scene) {
        if (scene != SignScene.NOTICE_ISSUE && scene != SignScene.NOTICE_MAKEUP) {
            return "病危重通知只支持「告知签发签名/告知补签」场景，当前场景「" + scene.getText() + "」不适用";
        }
        BizCriticalNotice n = noticeMapper.selectById(subject.bizId());
        if (n == null) {
            return "病危重通知单不存在或已被删除，无法签名";
        }
        if (Objects.equals(ObjectSignStatus.SIGNED.getCode(), n.getSignStatus())) {
            return "通知单 " + n.getNoticeNo() + " 已签名（签名即锁定），不能重复签名；"
                    + "如需修改，请先在「签名中心」作废该签名（作废会留痕并解除锁定）";
        }
        if (Objects.equals(NoticeStatusEnum.VOIDED.getCode(), n.getNoticeStatus())) {
            return "通知单 " + n.getNoticeNo() + " 已作废，不能签名";
        }
        if (Objects.equals(NoticeStatusEnum.ACKED.getCode(), n.getNoticeStatus())) {
            return "通知单 " + n.getNoticeNo() + " 已家属签收，内容冻结，不再接受签名";
        }
        if (scene == SignScene.NOTICE_ISSUE && Objects.equals(NoticeStatusEnum.ISSUED.getCode(), n.getNoticeStatus())) {
            return "通知单 " + n.getNoticeNo() + " 已签发（签名应已作废后重签），签发动作只做一次";
        }
        return null;
    }

    @Override
    public void applySignAnchor(Long bizId, SignScene scene, Long signId, LocalDateTime signedTime) {
        BizCriticalNotice patch = new BizCriticalNotice();
        patch.setId(bizId);
        patch.setSignStatus(ObjectSignStatus.SIGNED.getCode());
        patch.setSignId(signId);
        patch.setSignedTime(signedTime == null ? null : signedTime.truncatedTo(ChronoUnit.SECONDS));
        noticeMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizCriticalNotice n = noticeMapper.selectById(bizId);
        if (n == null || !Objects.equals(signId, n.getSignId())) {
            return;
        }
        noticeMapper.update(null, new LambdaUpdateWrapper<BizCriticalNotice>()
                .eq(BizCriticalNotice::getId, bizId)
                .set(BizCriticalNotice::getSignStatus, ObjectSignStatus.INVALIDATED.getCode())
                .set(BizCriticalNotice::getSignId, null));
        log.info("病危重通知签名已作废回写 noticeNo={} signId={}", n.getNoticeNo(), signId);
    }
}
