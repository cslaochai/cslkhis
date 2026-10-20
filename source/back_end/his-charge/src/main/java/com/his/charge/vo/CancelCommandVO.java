package com.his.charge.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账单撤销命令
 */
@Data
@NoArgsConstructor
public class CancelCommandVO {

    /**
     * 账单ID
     */
    private Long billId;

    /**
     * 退号原因（同时写进退款原因）
     */
    private String reason;

    /**
     * 操作人，服务端从当前登录用户取，不用前端传
     */
    private String operator;
}
