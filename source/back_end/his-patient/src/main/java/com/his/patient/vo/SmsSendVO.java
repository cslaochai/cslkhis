package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 验证码发送结果。
 */
@Data
public class SmsSendVO implements Serializable {

    /**
     * 联调回显的验证码：仅在 {@code sms.code.mock=true} 时返回，正式通道下恒为 null
     */
    private String mockCode;

    /**
     * 收码手机号的打码回显（如 138****8888），绑定场景用于提示码发往建档预留号码
     */
    private String phoneMask;
}
