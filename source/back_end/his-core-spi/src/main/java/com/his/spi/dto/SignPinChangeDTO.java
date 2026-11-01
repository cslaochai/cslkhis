package com.his.spi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改本人签名 PIN 入参：软签口令由签名人本人换掉，不许由他人代改。
 *
 * <p>证书行按当前登录人的员工ID定位，不由前端传，避免拿到别人的证书ID改他人口令。
 */
@Data
public class SignPinChangeDTO implements Serializable {

    /**
     * 当前签名 PIN（建号下发的初始口令也走这条路换掉）
     */
    @NotBlank(message = "当前签名PIN不能为空")
    @Size(min = 4, max = 32, message = "签名PIN长度须为4-32位")
    private String oldPin;

    /**
     * 新签名 PIN：明文只用于本次计算哈希，不入库、不落日志
     */
    @NotBlank(message = "新签名PIN不能为空")
    @Size(min = 4, max = 32, message = "签名PIN长度须为4-32位")
    private String newPin;
}
