package com.his.miniapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录入参（wx.login 的临时 code）。
 */
@Data
public class WxLoginDTO {

    @NotBlank(message = "code不能为空")
    private String code;
}
