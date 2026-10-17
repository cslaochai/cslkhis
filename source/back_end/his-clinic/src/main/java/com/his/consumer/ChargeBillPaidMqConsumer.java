package com.his.consumer;

import com.his.common.mq.HisMqTopics;
import com.his.common.mq.event.ChargeBillPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 账单已支付成功事件消费者。
 * <p>
 * 当前只做日志记录，作为下游 fan-out 的占位：
 * <ul>
 *   <li>后续接「收费成功微信通知」</li>
 *   <li>后续接「药房待摆药任务生成」</li>
 *   <li>后续接「工作台今日收入指标刷新」</li>
 * </ul>
 * 新增下游时不要改 PaymentServiceImpl，直接再加一个同 topic 不同 group 的消费者即可。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = HisMqTopics.CHARGE_BILL_PAID,
        consumerGroup = HisMqTopics.CG_CHARGE_BILL_PAID,
        selectorExpression = "bill_paid"
)
public class ChargeBillPaidMqConsumer implements RocketMQListener<ChargeBillPaidEvent> {

    @Override
    public void onMessage(ChargeBillPaidEvent event) {
        log.info("[收费事件] 账单 {} 患者 {} 支付成功 ¥{}（总额 ¥{}）流水 {} 笔 操作人 {}({})",
                event.getBillNo(), event.getPatientId(), event.getPaidAmount(),
                event.getTotalAmount(), event.getTxns() == null ? 0 : event.getTxns().size(),
                event.getCreateBy(), event.getCreateById());
        // 占位：后续在这里 fan-out 通知/指标/药房等下游
    }
}
