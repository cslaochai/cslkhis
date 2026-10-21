package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * DRG 结算默认版本 VO（GET /report/drg/version 响应）
 */
@Data
public class DrgVersionVO implements Serializable {

    /**
     * 库内 version 字符串
     */
    private String dbVersion;

    /**
     * 版本 Redis 缓存 key（his:drg:default-version）
     */
    private String redisKey;
}
