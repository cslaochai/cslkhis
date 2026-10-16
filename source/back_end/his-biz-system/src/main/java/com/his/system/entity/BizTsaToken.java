package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * 时间戳令牌台账（只增不改）。
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

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

}
