package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 报告 PDF 验签结果：文件未被篡改且签名值匹配证书公钥时 valid=true。
 */
@Data
public class ReportPdfVerifyVO {

    /**
     * 是否验签通过（文件未被篡改 + 签名值匹配签名证书公钥）
     */
    private boolean valid;

    /**
     * 验签结论文案（通过/失败原因，可直接展示）
     */
    private String message;
}
