package com.his.patient.service.impl;

import com.his.common.entity.SignSubject;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.mapper.BizInpatientRecordMapper;
import com.his.patient.service.InpatientRecordSignProvider;
import com.his.patient.support.InpatientRecordLabels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 住院病历文书的签名内容提供者（业务类型=1）。
 *
 * <p><b>规范化的两条硬规则</b>（写错任何一条都会让整套签名变成假警报）：
 *
 * <ol>
 *   <li><b>只放"文书内容"，绝不放流程字段</b>。{@code record_status / submit_time /
 *       archive_time / sign_status} 都会随流程变化 —— 放进去的话，医生一提交，
 *       提交签名当场验不过。这条踩过就会返工，所以在这里写死。</li>
 *   <li><b>字段顺序固定、null 与空串等价</b>，由 {@link CanonicalText} 保证。
 *       任何"按 Map 顺序拼"的写法都不可靠（HashMap 的遍历顺序不保证稳定）。</li>
 * </ol>
 *
 * <p><b>哪些文书可以签</b>：草案不能签（先提交）、已归档不能补签
 * （归档后补签会让"签名时刻"晚于"归档时刻"，制造时间线自相矛盾的证据）。
 * 会诊记录/转科记录/输血记录是各闭环完成时回写的系统文书，本期不接入自动签名 ——
 * 它们在覆盖率里会如实显示成"未签名"，不补签、不谎报。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InpatientRecordSignProviderImpl implements SignableContentProvider, InpatientRecordSignProvider {

    private final BizInpatientRecordMapper recordMapper;

    @Override
    public SignBizType bizType() {
        return SignBizType.INPATIENT_RECORD;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizInpatientRecord r = recordMapper.selectById(bizId);
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
                InpatientRecordLabels.recordStatusText(r.getRecordStatus()),
                InpatientRecordSignProvider.canonical(r));
    }

    @Override
    public String blockReason(SignSubject subject, SignScene scene) {
        BizInpatientRecord r = recordMapper.selectById(subject.bizId());
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
    public void applySignAnchor(Long bizId, SignScene scene, Long signId, LocalDateTime signedTime) {
        BizInpatientRecord r = new BizInpatientRecord();
        r.setId(bizId);
        r.setSignStatus(1);
        r.setSignId(signId);
        r.setSignedTime(signedTime == null ? null : signedTime.truncatedTo(ChronoUnit.SECONDS));
        recordMapper.updateById(r);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizInpatientRecord r = recordMapper.selectById(bizId);
        if (r == null || !Objects.equals(signId, r.getSignId())) {
            // 锚点指向的不是被作废的那条签名：不动它（说明后来又签过一次，不能误伤新签名）
            return;
        }
        // 置「签名已失效」而**保留 sign_id**：从病历能直接跳到那条签名去看"为什么被作废"。
        // 回落成 0-未签名是绝对不行的 —— 那会让"有人作废过签名"这个事实从列表上消失。
        BizInpatientRecord patch = new BizInpatientRecord();
        patch.setId(bizId);
        patch.setSignStatus(2);
        recordMapper.updateById(patch);
    }
}
