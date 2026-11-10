package com.his.spi.service;

import com.his.spi.vo.QrPayResultVO;

/**
 * 支付出码端口：结诊文书（引导单/处方签）在左上角渲染患者可扫的收款码。
 *
 * <p>只暴露「按结算账单出码」一个能力，出码的去重、节流、建单与码串缓存全部收在收费域实现内
 * （与收费台扫码收款同一入口、同一套口径，一张账单一张码），所以码形态由收费域开关决定：
 * 默认出支付宝原生码（仅支付宝可扫），关掉开关回到聚合码。
 */
public interface QrPayFacadeService {

    /**
     * 为结算账单出一张收款码（默认支付宝原生码，仅支付宝可扫；开关关闭时回到一码多平台的聚合码）。
     *
     * @param billId 结算账单ID；账单不存在/已作废/无应缴时抛业务异常
     * @return 出码结果（支付单号、账单号、金额、二维码图片 base64、过期时刻）
     */
    QrPayResultVO createQrCode(Long billId);
}
