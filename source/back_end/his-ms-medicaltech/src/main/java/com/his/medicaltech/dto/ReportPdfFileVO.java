package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报告 PDF 文件生成结果：路径 + 文件级数字签名元数据（落报告记录表）。
 */
@Data
public class ReportPdfFileVO {

    /**
     * PDF 相对路径（uploads/reportPdf/yyyyMMdd/{recordNo}.pdf）
     */
    private String relativePath;

    /**
     * 文件 SHA-256（十六进制小写）
     */
    private String fileHash;

    /**
     * 文件签名值（Base64，SHA256withRSA，签的是 "SHA-256:{hash}"）
     */
    private String signValue;

    /**
     * 签名证书ID（验签公钥来源）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certId;

    /**
     * 文件签名时刻
     */
    private LocalDateTime signedTime;
}
