package com.his.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 时间戳令牌台账（只增不改）。
 *
 * <p>TSA 服务视角的签发流水：盖一次章落一行，{@code serial} 唯一。
 * 签名证据行上冗余一份令牌值；两处不一致 = 被动过，
 * 验签会如实报失败（签名行那份用 TSA 公钥可独立验，台账删了也验得出真伪，
 * 但"台账缺失"本身要报出来 —— 那是有人在动证据）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_tsa_token")
public class BizTsaToken extends BaseEntity {

    /**
     * 令牌序列号（TSA + yyyyMMdd + 6位序号，唯一）
     */
    private String serial;

    /**
     * 被盖时间戳的内容摘要（SHA-256 十六进制小写）
     */
    private String digestHex;

    /**
     * TSA 授时时刻（服务端取值）
     */
    private LocalDateTime tsaTime;

    /**
     * 令牌值（TSA 私钥对规范化内容的 RSA 签名，Base64）
     */
    private String tokenValue;

    /**
     * 令牌签名算法
     */
    private String algo;
}
