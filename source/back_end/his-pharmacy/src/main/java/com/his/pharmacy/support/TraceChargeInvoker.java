package com.his.pharmacy.support;

import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.service.FeeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 高值耗材使用计费调用器 —— 让「记账失败」不会把「这件耗材用在谁身上了」一起拖回滚。
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
