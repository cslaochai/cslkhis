package com.his.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * TSA 服务启停入参（G6b 运维）。
 *
 * <p>停用的语义是「不再签发新令牌」：签名侧立即降级本机时钟（宁可承认不可信，
 * 也不谎报可信）；历史令牌凭已登记公钥仍可验证，不受停用影响。行不删除。
 */
@Data
public class TsaStatusUpsertDTO {

    /**
     * 目标状态（0-停用 1-启用，his_enable_status）
     */
    @NotNull(message = "目标状态不能为空")
    private Integer tsaStatus;
}
