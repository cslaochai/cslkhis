package com.his.platform.service;

import java.time.LocalDateTime;

/**
 * 可信时间戳通道：病历签名授时与令牌验真的时间来源出口，当前实现为 csssl 认证中心 TSA。
 */
public interface TsaChannelService {

    /**
     * 通道展示名称：就绪时取服务配置的名称，未就绪回内置缺省名。
     * 用于签名中心时间戳来源区块的名称展示。
     */
    String name();

    /**
     * 通道是否可用（csssl TSA 服务可达且公钥可取）；不可用时签名时间降级为本机时钟。
     * 用于签名中心状态卡与签名写入前决定时间来源。
     */
    boolean available();

    /**
     * 就绪时才给出通道名称，未就绪返回 null（页面按「未接入」展示）。
     * 用于签名中心状态卡区分「已接入」与「未接入」两种文案。
     */
    String readyName();

    /**
     * TSA 公钥指纹（每 4 位分组，便于电话核对）；未就绪返回 null。
     * 用于签名中心时间戳区块展示信任根指纹，人工核对 csssl 侧是否同一把密钥。
     */
    String fingerprintGroups();

    /**
     * 对内容摘要盖一枚可信时间戳：向 csssl 申请盖章并落台账。
     * 用于病历文书签名保存时确定签名时间来源（时间来源配为可信时间戳才走到这里）。
     *
     * @param digestHex 被盖时间戳的内容摘要（SHA-256 十六进制小写）
     * @return 序列号 / TSA 授时时刻 / 令牌值；返回 {@code null} = 无可用 TSA，调用方降级本机时钟
     */
    Stamp stamp(String digestHex);

    /**
     * 校验一枚令牌是否由本通道签发，且与摘要、授时时刻逐字对得上（异常一律按校验失败处理）。
     * 用于签名中心的令牌验真与病历签名记录的验签环节。
     */
    boolean verifyToken(String serial, String digestHex, LocalDateTime tsaTime, String tokenValue);

    /**
     * 失效通道内部的公钥缓存，下一次调用重新向 csssl 获取。
     * 用于 TSA 配置变更后让操作即时生效。
     */
    void invalidate();

    /**
     * 一次盖章的结果（三列缺一不可：签名行 time_source=3 时都要落库）
     */
    public record Stamp(String serial, LocalDateTime tsaTime, String tokenValue) {
    }
}
