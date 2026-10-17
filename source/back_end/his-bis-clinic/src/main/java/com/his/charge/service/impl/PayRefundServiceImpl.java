package com.his.charge.service.impl;

import com.his.charge.service.PayRefundService;
import com.his.common.enums.PaymentMethodEnum;
import com.his.common.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付渠道「原路退回」出口（M7 留口子）：收费侧退费的统一外发口。
 */
@Slf4j
@Service
public class PayRefundServiceImpl implements PayRefundService {

    /**
     * 幂等表：院内退费单号 → 渠道退费流水号（与真渠道"同 out_refund_no 只退一次"同构）
     */
    private final Map<String, String> issued = new ConcurrentHashMap<>();

    /**
     * 向渠道发起一笔退款。返回失败即视为"钱没退出去"，调用方必须回滚整笔退费，
     * 不能只把台账留着 —— 台账写出去就意味着账已经冲了。
     */
    public RefundReceipt refund(RefundRequest request) {
        if (request == null || !TextUtil.hasText(request.refundNo())) {
            return RefundReceipt.fail("缺少院内退费单号，渠道无法按单号幂等退款");
        }
        BigDecimal amount = request.amount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return RefundReceipt.fail("退款金额必须大于 0，当前 " + amount);
        }
        PaymentMethodEnum payMethod = PaymentMethodEnum.getByCode(request.payMethod());
        boolean channelBacked = payMethod != null && payMethod.channelBacked();
        if (!channelBacked) {
            // 走到这里说明调用方绕过了 RefundMethodEnum.viaChannel：现金/余额/医保不该出现在商户平台。
            // 渠道三兄弟（微信/支付宝/银行卡）一律受理——银行卡是 POS 刷卡收款，退款走 POS 冲正，
            // 同样必须经过渠道口子；在这里拒掉它，等于让"原路退回"的口径三处打架（枚举说退、渠道说不退）。
            return RefundReceipt.fail("支付方式「" + (payMethod == null ? request.payMethod() : payMethod.getDesc())
                    + "」不走商户平台原路退回");
        }

        String existing = issued.get(request.refundNo());
        if (existing != null) {
            log.info("[M7渠道退费口子] 幂等命中：退费单号 {} 已受理过，沿用渠道流水号 {}，不重复退款",
                    request.refundNo(), existing);
            return RefundReceipt.ok(existing);
        }

        // —— M7 留口子：这一段打印就是"调商户平台退款接口"的占位，真渠道接入后整块替换 ——
        String channelRefundNo = "SIMR-" + payMethod.getCode() + "-" + request.refundNo();
        issued.put(request.refundNo(), channelRefundNo);
        log.info("[M7渠道退费口子] ===== 模拟调商户平台原路退回 ===== 渠道={} 原收费单号={} 退费单号={} 金额={} 原因={} → 渠道退费流水号={}",
                payMethod.getDesc(), request.chargeNo(), request.refundNo(), amount,
                TextUtil.hasText(request.reason()) ? request.reason() : "收费处退费", channelRefundNo);
        return RefundReceipt.ok(channelRefundNo);
    }
}
