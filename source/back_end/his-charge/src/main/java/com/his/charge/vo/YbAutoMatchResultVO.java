package com.his.charge.vo;

import lombok.Data;

/**
 * 自动对照结果（值即结论：matched+ambiguous+skipped = 参与比对的未对照数）。
 */
@Data
public class YbAutoMatchResultVO {

    /**
     * 本轮自动对照成功条数
     */
    private Integer matched;

    /**
     * 名称命中多条目录（歧义）跳过条数
     */
    private Integer ambiguous;

    /**
     * 名称未命中任何启用目录条数
     */
    private Integer noMatch;

    /**
     * 自动对照前未对照总数
     */
    private Integer beforeTotal;

    /**
     * 自动对照后仍未对照总数
     */
    private Integer afterTotal;
}
