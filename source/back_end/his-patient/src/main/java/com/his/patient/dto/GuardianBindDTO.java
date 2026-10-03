package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 绑定已有就诊人入参（姓名 + 身份证 + 建档预留手机号上的短信码，三要素四因子校验）。
 *
 * <p>收码手机号由服务端按姓名+身份证定位档案后取自建档预留号码，<b>不由前端传入</b> ——
 * 能收到码即视为本人（或紧密家属）授权，前端连收码号码是什么都看不到（只在发码后回显打码值）。
 */
@Data
public class GuardianBindDTO implements Serializable {

    /** 患者姓名 */
    @NotBlank(message = "请输入就诊人姓名")
    private String patientName;

    @NotBlank(message = "请输入身份证号")
    private String idCard;

    @NotBlank(message = "请输入短信验证码")
    private String smsCode;

    /** 关系码值（字典患者关系字典），必填 */
    @NotNull(message = "请选择与就诊人的关系")
    private Integer relation;
}
