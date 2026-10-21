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
}
