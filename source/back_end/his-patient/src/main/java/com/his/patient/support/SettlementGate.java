package com.his.patient.support;

import com.his.charge.api.InpatientSettlementGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 出院结算闸门 —— 把"有没有结算"这件事从收费模块里问出来。
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
