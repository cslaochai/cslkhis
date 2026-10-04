package com.his.system.vo;

import lombok.Data;

/**
 * 登录口令加密公钥出参。
 *
 * <p>匿名可取（走登录白名单）：公钥本来就是公开信息，它的作用是让口令在离开浏览器前就变成密文。
 * 私钥永远不出后端，见 {@code his.security.sm2}。
 */
@Data
public class PublicKeyVO {

    /**
     * 公钥指纹（前 16 位），前端据此判断公钥是否已轮换
     */
    private String keyId;

    /**
     * SM2 公钥（16 进制，130 字符，含 04 未压缩点前缀）
     */
    private String publicKey;

    /**
     * 算法标识
     */
    private String algorithm = "SM2";

    /**
     * 密文块顺序，必须与后端 BouncyCastle 的 SM2Engine.Mode 对齐
     */
    private String mode = "C1C3C2";
}
