package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * DIP 结算默认病种库版本 VO（GET /report/dip/version 响应）。
 */
@Data
public class DipVersionVO implements Serializable {

    /**
     * 库内 version 字符串
     */
    private String dbVersion;

    /**
     * 版本 Redis 缓存 key（his:dip:default-version）
     */
    private String redisKey;
}
