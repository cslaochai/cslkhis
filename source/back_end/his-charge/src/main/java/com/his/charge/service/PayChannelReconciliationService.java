package com.his.charge.service;

import java.time.LocalDate;

/**
 * 支付渠道对账服务（拉微信/支付宝账单 + 自动勾对）
 */
public interface PayChannelReconciliationService {

    /**
     * 拉取指定日期的渠道账单并入库
     *
     * @param channel 渠道（1-微信 2-支付宝）
     * @param billDate 账单日期
     * @return 导入笔数
     */
    int importChannelBill(Integer channel, LocalDate billDate);

    /**
     * 自动勾对指定日期的账单
     *
     * @param channel 渠道（1-微信 2-支付宝）
     * @param billDate 账单日期
     * @return 勾对成功的笔数
     */
    int autoMatch(Integer channel, LocalDate billDate);

    /**
     * 手工登记一笔渠道流水（用于渠道账单缺失时的应急补录）
     */
    void manualRegister(com.his.charge.dto.PayChannelManualDTO dto);
}
