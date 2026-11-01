package com.his.spi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 签名 CA 通道状态：证书签发走在线申请（csssl RA）还是线下人工流程的实时自检回参。
 */
@Data
public class SignCaStatusVO implements Serializable {

    /**
     * CA 交互方式（online-在线申请；offline-线下人工：HIS 生成密钥对、申请单线下送 CA、回填证书）
     */
    private String caMode;

    /**
     * 是否可用：在线模式下表示 CA 连通且模板可用；线下人工模式恒为 true
     */
    private Boolean available;

    /**
     * 当前签发行为提示文案（连通性、模板与算法匹配、线下兜底口径）
     */
    private String hint;

    /**
     * CA 侧租户 App ID（非密钥，仅用于核对接入的是哪个租户；未接入为空）
     */
    private String appId;

    /**
     * 在线申请使用的证书模板编码
     */
    private String templateCode;

    /**
     * HIS 新证书密钥算法（RSA2048 / SM2）
     */
    private String keyAlgorithm;
}
