package com.his.spi.service;

import com.his.spi.vo.TsaStampVO;

import java.time.LocalDateTime;

/**
 * 可信时间戳端口：签名授时与令牌验真的唯一出口，时间来源事实由签名域持有。
 */
public interface TsaStampFacadeService {

    /**
     * 对内容摘要盖一枚可信时间戳并落台账，序列号冲突自动重试。
     *
     * <p>用于病历签名保存时确定签名时刻的来源；返回 null 表示没有可用时间戳服务，调用方降级本机时钟。
     */
    TsaStampVO stamp(String digestHex);

    /**
     * 校验一枚令牌是否由本通道签发，且与摘要、授时时刻逐字对得上。
     *
     * <p>用于签名记录的验签环节与时间戳台账复验；异常一律按校验不通过处理。
     */
    boolean verifyToken(String serial, String digestHex, LocalDateTime tsaTime, String tokenValue);

    /**
     * 时间戳通道是否可用（服务已配置且启用、签发密钥可解出）。
     */
    boolean available();

    /**
     * 通道就绪时的展示名称；未就绪返回 null（页面按「未接入」展示）。
     */
    String readyName();

    /**
     * 最终生效的签名时间来源：配置值与通道在线状态共同决定，全院唯一口径。
     *
     * <p>签名落库的 time_source 与签名概览说明都读这里。
     */
    int effectiveTimeSource();

    /**
     * 时间戳台账总条数。
     */
    long countTokens();
}
