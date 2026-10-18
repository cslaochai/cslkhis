package com.his.system.vo;

import lombok.Data;

import java.io.Serializable;

/** 签名 CA 通道状态：证书签发走院内自签还是外部 CA 的实时自检回参。 */
@Data
public class SignCaStatusVO implements Serializable {

    /** 证书签发模式（internal-院内自签 external-外部 CA） */
    private String caMode;

    /**
     * external 模式下的适配器名，internal 为 null
     */
    private String providerName;

    /** 外部 CA 通道是否就绪（仅配置为 external 模式时为真） */
    private Boolean available;

    /** 当前模式的签发行为提示文案（信任根与中断/回退口径） */
    private String hint;
}
