package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 短信验证码发送入参（小程序端）。
 *
 * <p>场景码由服务端固定写死，不接受前端传入，避免匿名接口被用来发其他业务的码。
 */
@Data
public class SmsCodeSendDTO implements Serializable {

    /** 联系电话（快照） */
    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
