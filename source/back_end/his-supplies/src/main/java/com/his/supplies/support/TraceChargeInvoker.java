package com.his.supplies.support;

import com.his.fee.dto.FeeBookDTO;
import com.his.fee.entity.BizFeeRecord;
import com.his.fee.service.FeeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 高值耗材使用计费调用器 —— 让「记账失败」不会把「这件耗材用在谁身上了」一起拖回滚。
 *
 * <p>与 {@code TreatmentChargeInvoker} 同一个道理：记账若与登记同事务（REQUIRED），
 * 一抛异常外层事务被标 rollback-only，提交时 UnexpectedRollbackException，
 * 台账和失败原因全丢。REQUIRES_NEW 后内层失败只回滚内层，登记与计费失败留痕照常提交。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TraceChargeInvoker {

    private final FeeRecordService feeRecordService;

    /**
     * 一件一笔记账（独立事务，失败不回滚使用登记）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BizFeeRecord book(FeeBookDTO dto) {
        return feeRecordService.book(dto);
    }
}
