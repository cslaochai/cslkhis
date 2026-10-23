package com.his.spi.service;

import com.his.spi.vo.QrPayResultVO;

/**
 * 聚合支付出码端口：结诊文书（引导单/处方签）在左上角渲染患者可扫的聚合收款码。
 *
 * <p>只暴露「按结算账单出码」一个能力，出码的去重、节流、建单与码串缓存全部收在收费域实现内
 * （与收费台扫码收款同一入口、同一套口径，一张账单一张码）。
 */
public interface QrPayFacadeService {

    /**
     * 为结算账单出一张聚合收款码（微信/支付宝/云闪付同一张码，患者扫码付款）。
     *
     * @param billId 结算账单ID；账单不存在/已作废/无应缴时抛业务异常
     * @return 出码结果（支付单号、账单号、金额、二维码图片 base64、过期时刻）
     */
    QrPayResultVO createQrCode(Long billId);
}
