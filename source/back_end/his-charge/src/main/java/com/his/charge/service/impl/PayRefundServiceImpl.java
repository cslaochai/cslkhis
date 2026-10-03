package com.his.charge.service.impl;

import com.his.charge.service.PayRefundService;
import com.his.common.enums.PaymentMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付渠道「原路退回」出口（M7 留口子）：收费侧退费的统一外发口。
 *
 * <p><b>当前没有商户账号，所以是真一半假一半</b>：真的是入参口径、失败口径与幂等键
 * （以院内退费单号为准，同一单号重复请求返回同一个渠道流水号，不退两次）；
 * 假的是退款并没有真的发生在微信/支付宝侧。因此返回的 SIMR- 号码<b>不能作为资金核对依据</b>，
 * 它只把"退钱的请求发出去了"这条链路跑真。
 *
 * <p>它与 {@code MiniappPayServiceImpl.refundByBiz} 里那句"[微信支付口子]"的区别：
 * 那边只服务患者自助付的那批单（有患者端统一支付单记录），
 * 窗口收的现金/扫码钱从来没有交易号，退费时完全无处发请求。
 * 真接入时在本类内按 {@code payMethod} 分发到商户平台退款接口，调用方与台账都不动。
 *
 * <p><b>只有在线渠道会调到这里</b>：现金柜面点钞、余额退回患者账户、医保走 2305 撤销报文，
 * 都不该把请求发给商户平台（判据在 {@code RefundMethodEnum.viaChannel}，别在调用方另写一份）。
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
        if (request == null || !StringUtils.hasText(request.refundNo())) {
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
                StringUtils.hasText(request.reason()) ? request.reason() : "收费处退费", channelRefundNo);
        return RefundReceipt.ok(channelRefundNo);
    }
}
