package com.his.emr.support;

import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.service.FeeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 门诊治疗按次记账调用器 —— 让「记账失败」不会把「这次治疗做过了」一起拖回滚。
 *
 * <p>不加这层会怎样：记账与打卡在<b>同一个事务</b>里（REQUIRED），记账一抛异常 Spring 就把整个
 * 事务标成 rollback-only，调用方的 try-catch 拦不住 —— 提交时抛
 * {@code UnexpectedRollbackException}，现象是「打卡接口 500，执行流水和计费失败原因全丢」，
 * 与"记账失败要留痕、给补记留入口"的设计意图正好相反。
 * REQUIRES_NEW 之后内层失败只回滚内层，外层的打卡与失败留痕照常提交。
 * 独立成 Bean 是因为 {@code @Transactional} 走代理，同类自调用注解不生效。
 *
 * <p>定位信息取不到时<b>返回 {@code null} 而不是抛异常</b>：抛出来会冒泡成打卡接口的 500，
 * 而"这笔钱先记不上"是允许的状态 —— 治疗站把行标成「计费失败」并留下补记入口。
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
        if (!StringUtils.hasText(dto.getPatientNo()) || !StringUtils.hasText(dto.getPatientName())) {
            log.warn("门诊治疗记账缺少患者编号/姓名快照，本次不计费（registId={} 来源单 {}）",
                    dto.getEncounterId(), dto.getSourceNo());
            return null;
        }
        return feeRecordService.book(dto);
    }
}
