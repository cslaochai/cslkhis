package com.his.patient.support;

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
 * 医嘱执行记账调用器 —— 让"记账失败"不会把"医嘱执行"一起拖回滚。
 *
 * <p><b>为什么需要这个类</b>：医嘱执行与记账必须在同一个业务动作里做，但两者的
 * "失败代价"完全不同 —— 护士打针这件事**已经发生了**（执行记录必须留下），
 * 而记账失败只是账没记上。如果把它们放在同一个事务里，记账抛异常会连执行记录一起回滚，
 * 结果就是"护士做了，系统说没做"。
 *
 * <p>Spring 的默认传播（REQUIRED）下，内层抛 RuntimeException 会把**共享事务标记为
 * rollback-only**，外层即便 catch 住也无法提交（提交时抛 UnexpectedRollbackException）——
 * 所以这里必须用 {@code REQUIRES_NEW} 开一个独立事务：内层失败只回滚内层。
 *
 * <p>独立成 Bean 是必须的：Spring 的 {@code @Transactional} 基于代理，
 * **同一个类内部自调用注解不生效**。
 *
 * <p>定位信息取不到时<b>返回 {@code null} 而不是抛异常</b>：抛出来会把"护士已经执行过了"
 * 一起回滚成 500，而"这笔钱先记不上"是允许的状态 —— 执行行会写明未记账原因，留出补记入口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderChargeInvoker {

    private final FeeRecordService feeRecordService;

    /**
     * 一次执行记一行（独立事务）
     *
     * @return 记账行；{@code null} = 本次未记账，**调用方必须留痕说明**
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BizFeeRecord book(FeeBookDTO dto) {
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
        return feeRecordService.book(dto);
    }
}
