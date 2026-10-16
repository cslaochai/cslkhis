package com.his.charge.service;

import com.his.charge.dto.QrPayCreateDTO;
import com.his.charge.vo.QrPayCreateVO;
import com.his.charge.vo.QrPayResultVO;

import java.util.Map;

/**
 * 收费台扫码收款（医院出码、患者扫）：把「账单 → 支付单 → 渠道出码 → 到账入账」串起来。
 *
 * <p>与柜面现金/扫码枪（付款码被扫）是两条相反的链路：被扫是收银员扫患者的付款码，
 * 当场就知道结果；主扫由患者手机扫码后自己付，结果只能靠渠道通知或前端轮询回来。
 */
public interface QrPayService {

    /**
     * 按账单出码（金额取账单实时应缴，前端传不进金额）。
     */
    QrPayCreateVO create(QrPayCreateDTO dto);

    /**
     * 轮询收款结果：渠道查单是收款事实的来源，通知只是加速。
     * 渠道已实收而本地未入账时，本方法兜底入账 —— 公网回调断了也不会漏账。
     */
    QrPayResultVO queryResult(String payNo);

    /**
     * 关单（收银员取消本次收款）：本地置已关闭 + 尽力关闭渠道侧交易。
     */
    void close(String payNo);

    /**
     * 模拟收款（渠道处于模拟模式时的验收入口）：把「患者已照单付款」这件事登记进模拟渠道，
     * 之后前端轮询查单就会按实收入账。真模式调用一律拒绝。
     */
    void mockMarkPaid(String payNo);

    /**
     * 支付宝异步通知（收费侧）：应答体是渠道协议要求的裸文本，不是统一响应结构。
     */
    String confirmByAlipayNotify(Map<String, String> params);

    /**
     * 微信异步通知（收费侧）。通道未接通，一律不受理。
     */
    String confirmByWxNotify(String body);
}
