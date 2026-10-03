package com.his.operation.support;

import com.his.fee.dto.FeeBookDTO;
import com.his.fee.entity.BizFeeRecord;
import com.his.fee.service.FeeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 手术麻醉记账调用器 —— 让"记账失败"不会把"麻醉记录已经提交"一起拖回滚。
 *
 * <p>与 {@code OrderChargeInvoker} 同一个道理：麻醉这件事**已经发生了**（记录单已提交、
 * 体征用药已固化），记账失败只是账没记上。REQUIRES_NEW 让内层失败只回滚内层；
 * 独立成 Bean 是因为 Spring 的 {@code @Transactional} 基于代理，同类内部自调用注解不生效。
 *
 * <p>返回 {@code null} 在本类不会出现（记账要么成功要么抛），调用方仍按「拿不到记账行
 * 就标计费失败并写原因」处理 —— 没计上就必须显示没计上。
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
