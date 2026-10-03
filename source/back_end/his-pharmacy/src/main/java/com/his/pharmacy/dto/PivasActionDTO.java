package com.his.pharmacy.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 静配流转操作入参（打标签排队 / 调配 / 核对发放）。
 *
 * <p>打标签排队按主单（batchId 必填，整单已审方明细一次取号）；调配/核对按明细（itemId 必填）。
 * 操作人一律服务端取当前登录人。
 */
@Data
public class PivasActionDTO implements Serializable {

    /**
     * 主单ID（打标签排队用）
     */
    private Long batchId;

    /**
     * 明细ID（调配/核对用）
     */
    private Long itemId;

    /**
     * 备注（选填）
     */
    private String remark;
}
