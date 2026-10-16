package com.his.patient.support;

import com.his.api.dto.FeeBookDTO;
import com.his.api.service.ChargeApi;
import com.his.api.vo.FeeRecordVO;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 医嘱执行记账调用器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderChargeInvoker {

    private final ChargeApi chargeApi;

    /**
     * 一次执行记一行（独立事务）
     *
     * @return 记账行；{@code null} = 本次未记账，**调用方必须留痕说明**
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public FeeRecordVO book(FeeBookDTO dto) {
        if (dto.getEncounterId() == null) {
            log.warn("住院医嘱记账缺少入院ID，记账行无法归属这次住院，本次不记账（来源单 {}）", dto.getSourceNo());
            return null;
        }
        // 幂等锚点：一次执行一行。缺了来源ID 就只能按医嘱判重，长期医嘱会静默少收
        if (dto.getSourceId() == null) {
            log.warn("住院医嘱记账缺少执行记录ID（幂等锚点），本次不记账（admissionId={} 来源单 {}）",
                    dto.getEncounterId(), dto.getSourceNo());
            return null;
        }
        // 患者姓名是记账行的必填快照，缺了不是难看而是整条写入失败
        if (dto.getPatientId() == null || !TextUtil.hasText(dto.getPatientName())) {
            log.warn("住院医嘱记账缺少患者快照，本次不记账（admissionId={} 来源单 {}）",
                    dto.getEncounterId(), dto.getSourceNo());
            return null;
        }
        return chargeApi.bookFee(dto);
    }
}
