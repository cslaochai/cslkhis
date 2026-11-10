package com.his.charge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 聚合支付码（医院自有聚合网关）配置。
 *
 * <p>聚合码不绑定单一渠道：同一张码微信、支付宝、云闪付都能扫，渠道在支付那一刻才由网关识别确定。
 * 因此聚合码本身不进任何渠道的预下单，只编码「医院聚合收款页地址 + 支付单号」，患者扫后由网关拉起对应 App 付款。
 */
@Data
@Component
@ConfigurationProperties(prefix = "pay.aggregated")
public class PayAggregatedProperties {

    /**
     * 聚合收款页地址前缀（医院自有网关）。拼接支付单号即患者可扫的收款链接。
     * dev 用占位域名；生产替换为医院真实可访问的 HTTPS 域名。
     */
    private String qrBaseUrl = "https://pay.hospital.com/qr/";

    /**
     * 是否走模拟收款：true 时无真实网关，出占位码、靠 {@code /qrPay/mockPaid} 触发整条入账链路做验收；
     * false 时依赖真实网关的异步通知入账，本端不做任何模拟入账。
     */
    private boolean mockEnabled = true;

    /**
     * 收费台出的码是否改为支付宝原生支付码（当面付 {@code alipay.trade.precreate}）。
     *
     * <p>true：码串换成支付宝返回的 {@code qr_code}，建单时渠道即定为支付宝；这张码只能被支付宝扫，
     * 微信/云闪付扫不开 —— 即「名义聚合、实为支付宝单渠道」的落地形态，用于未接真实聚合网关的阶段。
     * false：维持聚合码形态（码串 = {@link #qrBaseUrl} + 支付单号，渠道留空待网关识别）。
     *
     * <p>默认开启：医疗场景不需要真聚合，收款就出支付宝码（收费台、床旁、结诊引导单同一入口）。
     * pro 用 {@code ALIPAY_QR_ENABLED=false} 显式关掉，直到当面付签约、回调公网可达再切。
     */
    private boolean alipayQrEnabled = true;
}
