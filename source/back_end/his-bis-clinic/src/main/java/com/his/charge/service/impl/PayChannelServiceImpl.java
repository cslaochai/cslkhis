package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.service.PayChannelService;
import com.his.common.enums.PayTxnStatusEnum;
import com.his.common.enums.PaymentMethodEnum;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付渠道账单出口（M7 留口子）：拉取"对方账"给 reconcile 侧勾对。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayChannelServiceImpl extends ServiceImpl<BizPaymentTxnMapper, BizPaymentTxn> implements PayChannelService {

    private final BizPaymentTxnMapper bizPaymentTxnMapper;

    /**
     * 拉取指定渠道、指定账单日的渠道侧流水。
     * 渠道流水号由渠道侧保证在 (channel, tradeNo) 维度唯一 —— 台账用它做幂等键。
     */
    public List<ChannelTrade> fetchChannelBill(Integer channel, LocalDate billDate) {
        // —— M7 留口子：这一段打印就是"调渠道接口"的占位，真渠道接入后整块替换 ——
        log.info("[M7支付渠道口子] ===== 模拟调商户平台拉取账单 ===== 渠道={} 账单日={}",
                PaymentMethodEnum.labelOrUnknown(channel), billDate);

        // 反造口径：本地当日、该渠道、成功状态的支付流水，逐笔生成渠道流水。
        // 已冲正流水（txn_status=2）不进账单 —— 真渠道也不会为它出一笔钱。
        // 金额带符号照抄：退费在渠道侧同样是一笔负数，勾对时两侧同符号才比得上。
        List<BizPaymentTxn> txns = bizPaymentTxnMapper.selectList(new LambdaQueryWrapper<BizPaymentTxn>()
                .eq(BizPaymentTxn::getPayMethod, channel)
                .eq(BizPaymentTxn::getTxnStatus, PayTxnStatusEnum.SUCCESS.getCode())
                .eq(BizPaymentTxn::getTxnDate, billDate)
                .orderByAsc(BizPaymentTxn::getTxnTime));

        List<ChannelTrade> trades = new ArrayList<>();
        for (BizPaymentTxn txn : txns) {
            // 收款时已按 SIMU- 前缀记在 channel_txn_no 上（PaymentServiceImpl.channelNoOf），
            // 真渠道就是那个位置存商户平台流水号，所以这里直接复用，不另造一套号。
            String tradeNo = TextUtil.hasText(txn.getChannelTxnNo())
                    ? txn.getChannelTxnNo()
                    : "SIMU-" + channel + "-" + billDate.format(DateFormats.DATE) + "-" + txn.getTxnNo();
            trades.add(new ChannelTrade(tradeNo, txn.getTxnTime(), txn.getAmount()));
            log.info("[M7支付渠道口子] 渠道流水 tradeNo={} amount={}（对应本地支付流水 {}）",
                    tradeNo, txn.getAmount(), txn.getTxnNo());
        }
        log.info("[M7支付渠道口子] ===== 拉取完成 ===== 渠道={} 账单日={} 共 {} 笔", PaymentMethodEnum.labelOrUnknown(channel), billDate, trades.size());
        return trades;
    }
}
