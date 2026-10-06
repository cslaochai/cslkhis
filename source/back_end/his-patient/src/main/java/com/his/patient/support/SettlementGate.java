package com.his.patient.support;

import com.his.charge.service.InpatientSettlementGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 出院结算闸门 —— 把"有没有结算"这件事从收费模块里问出来。
 *
 * <p>独立成 Bean 是因为"收费模块缺席时怎么办"这件事必须只有一处实现，
 * 否则每加一个调用点就多一种降级口径。
 *
 * <p>降级口径：<b>缺席则放行，但必须留痕"未校验"</b> ——
 * 不能为了"业务不卡住"就假装校验通过（同「发送方 status=1 只证明我发过」的口径）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementGate {

    private final ObjectProvider<InpatientSettlementGateway> settlementGateway;

    /**
     * 收费模块是否已接入
     */
    public boolean available() {
        return settlementGateway.getIfAvailable() != null;
    }

    /**
     * 查询结算状态（收费模块缺席返回 {@code null}）
     */
    public InpatientSettlementGateway.SettlementState state(Long admissionId) {
        InpatientSettlementGateway gateway = settlementGateway.getIfAvailable();
        if (gateway == null) {
            return null;
        }
        try {
            return gateway.state(admissionId);
        } catch (Exception e) {
            // 查不到结算状态不能直接放行：让调用方按"未校验"处理并留痕
            log.error("查询住院结算状态失败，admissionId={}", admissionId, e);
            return null;
        }
    }
}
