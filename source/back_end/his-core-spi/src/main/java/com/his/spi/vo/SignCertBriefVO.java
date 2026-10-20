package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 签名证书摘要：跨模块完成「用哪张证书签、拿公钥验」所需的最小字段集。
 */
@Data
public class SignCertBriefVO {

    /**
     * 证书ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 证书编号
     */
    private String certNo;

    /**
     * 公钥（PEM），验签用
     */
    private String publicKey;
}
