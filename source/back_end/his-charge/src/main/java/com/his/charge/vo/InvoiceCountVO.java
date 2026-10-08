package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 票据张数聚合（开票数 + 作废数），对应两条同构 SQL：
 */
@Data
public class InvoiceCountVO implements Serializable {

    /**
     * 票据总张数（{@code COUNT(*)}；MySQL BIGINT）
     */
    private Long cnt;

    /**
     * 作废张数（含 3-已作废与 4-已红冲换开，两者都是交不出去的废票根）
     */
    private Long voidCnt;
}