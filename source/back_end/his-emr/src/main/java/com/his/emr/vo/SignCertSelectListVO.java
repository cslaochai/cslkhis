package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 签名证书下拉出参：只回答「这是谁的哪本证书、还能不能用」。
 */
@Data
@Schema(name = "SignCertSelectListVO", description = "签名证书下拉出参")
public class SignCertSelectListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 证书编号
     */
    private String certNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long empId;

    private String empName;

    /**
     * 证书状态（1-有效 2-已吊销 3-已过期）
     */
    private Integer certStatus;

    private String certStatusText;
}
