package com.his.emr.support;

import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.service.FeeRecordService;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 门诊治疗按次记账调用器 —— 让「记账失败」不会把「这次治疗做过了」一起拖回滚。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TreatmentChargeInvoker {

    private final FeeRecordService feeRecordService;

    /**
     * 按次记账（独立事务，失败不回滚打卡）
     *
     * @return 记账行；{@code null} = 本次未计费
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BizFeeRecord book(FeeBookDTO dto) {
        if (dto.getEncounterId() == null) {
            log.warn("门诊治疗记账缺少挂号ID，无法定位本次就诊的记账行，本次不计费（项目 {}）", dto.getItemCode());
            return null;
        }
        // 幂等键是 (来源, sourceId, 项目码)：sourceId 用执行流水行ID，一次打卡一行。
        // 缺了它同一疗程第 2 次以后的真账会被判成重复记账直接抹掉。
        if (dto.getSourceId() == null) {
            log.warn("门诊治疗记账缺少执行流水ID（幂等锚点），本次不计费（registId={} 项目 {}）",
                    dto.getEncounterId(), dto.getItemCode());
            return null;
        }
        // 患者编号/姓名是记账行的必填快照，缺了不是"记一笔难看的账"而是整条写入失败
        if (!TextUtil.hasText(dto.getPatientNo()) || !TextUtil.hasText(dto.getPatientName())) {
            log.warn("门诊治疗记账缺少患者编号/姓名快照，本次不计费（registId={} 来源单 {}）",
                    dto.getEncounterId(), dto.getSourceNo());
            return null;
        }
        return feeRecordService.book(dto);
    }
}
