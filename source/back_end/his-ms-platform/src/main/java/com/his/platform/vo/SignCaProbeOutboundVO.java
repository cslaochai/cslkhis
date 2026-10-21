package com.his.platform.vo;

import lombok.Data;

import java.io.Serializable;

/** 证书机构外发探针结果：一次外发试跑回显的通道事实，不落任何库表痕迹。 */
@Data
public class SignCaProbeOutboundVO implements Serializable {

    /** 证书签发适配器名称 */
    private String providerName;

    /** 证书签名请求是否已按适配器口径提交并打印到服务端控制台 */
    private Boolean csrPrinted;

    /** 适配器是否返回了证书（未接入真证书机构时恒为 false） */
    private Boolean certReturned;

    /** 探针结论：给对接人员看的外发通道说明 */
    private String conclusion;
}
