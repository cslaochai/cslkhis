package com.his.spi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按台账ID复验时间戳令牌入参（运维）：对台账中的一枚令牌重新执行验证。
 */
@Data
public class TsaTokenVerifyDTO {

    /**
     * 台账行 ID（时间戳令牌台账主键）
     */
    @NotNull(message = "令牌 ID 不能为空")
    private Long id;
}
