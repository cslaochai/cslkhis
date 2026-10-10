package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时间戳令牌台账行（只增不改）。
 */
@Data
public class TsaTokenVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 令牌序列号
     */
    private String serial;

    /**
     * 被盖时间戳的内容摘要
     */
    private String digestHex;

    /**
     * TSA 授时时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tsaTime;

    /**
     * 令牌值（Base64）
     */
    private String tokenValue;

    /**
     * 令牌签名算法
     */
    private String algo;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;
}
