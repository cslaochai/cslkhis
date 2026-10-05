package com.his.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时间戳令牌复验结果（G6b 运维）。
 *
 * <p>{@code valid} 是唯一结论：令牌签名值可用 TSA 公钥验证、且与台账摘要一致。
 * 失败时 {@code failReason} 给出人话原因（令牌不存在 / 摘要不一致 / 服务未配置 / 签名不匹配）。
 */
@Data
public class TsaTokenVerifyVO {

    /**
     * 台账行 ID（BIGINT 走字符串序列化，防 JS 精度丢失）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 令牌序列号
     */
    private String serial;

    /**
     * 被盖时间戳的内容摘要
     */
    private String digestHex;

    /**
     * TSA 授时时刻
     */
    private LocalDateTime tsaTime;

    /**
     * 令牌签名算法
     */
    private String algo;

    /**
     * 复验结论：true=令牌可验（签名值+摘要+时刻全对得上）
     */
    private Boolean valid;

    /**
     * 失败原因（valid=true 时为 null）
     */
    private String failReason;

    private LocalDateTime verifyTime;
}
