package com.his.operation.support;

import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.service.FeeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 手术麻醉记账调用器 —— 让"记账失败"不会把"麻醉记录已经提交"一起拖回滚。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationChargeInvoker {

    private final FeeRecordService feeRecordService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BizFeeRecord book(FeeBookDTO dto) {
        return feeRecordService.book(dto);
    }
}
