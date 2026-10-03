package com.his.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 签名时间来源切换入参（G6b 运维）。
 *
 * <p>只允许 1（本机时钟）/ 3（可信时间戳）。2（院内授时服务器）**没有对应实现**，
 * 配了也只会按本机时钟跑——按「没有实现就绝不能写这个值」的铁律，接口直接拒绝。
 */
@Data
public class TsaTimeSourceDTO {

    /** 时间来源（1-本机时钟 2-院内授时服务器 3-第三方TSA） */
    @NotNull(message = "时间来源不能为空")
    private Integer timeSource;
}
