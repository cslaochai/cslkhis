package com.his.platform.service;

/**
 * 短信验证码服务：按「手机号 × 业务场景」发放并核验一次性验证码。
 * 内部接口：继承对外窄端口 {@link com.his.spi.service.SmsCodeService}（含 SendResult record）。
 */
public interface SmsCodeService extends com.his.spi.service.SmsCodeService {
}
