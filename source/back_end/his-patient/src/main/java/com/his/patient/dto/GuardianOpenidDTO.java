package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 当前账号绑定微信 openid 入参（小程序 wx.login/授权后回传）
 */
@Data
public class GuardianOpenidDTO implements Serializable {

    @NotBlank(message = "openid不能为空")
    private String openid;
}
