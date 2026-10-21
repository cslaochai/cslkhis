package com.his.spi.service;

/**
 * 登录口令 SM2 解密窄端口：把前端密文还原成明文口令。
 */
public interface PasswordCipherFacadeService {

    /**
     * 解密前端传来的口令密文。
     *
     * @param cipherText sm-crypto 的 {@code doEncrypt} 输出（十六进制，不带 04 前缀）
     * @return 明文口令
     */
    String decrypt(String cipherText);
}
