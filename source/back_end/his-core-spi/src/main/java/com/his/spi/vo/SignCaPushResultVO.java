package com.his.spi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * CA 状态推送的处理结果：是否接收、是否幂等重放、最终落到哪个申请状态。
 */
@Data
public class SignCaPushResultVO implements Serializable {

    /**
     * 是否接收（状态机允许且处理完成）
     */
    private Boolean accepted;

    /**
     * 是否幂等重放：同一状态重复推送，未做任何变更
     */
    private Boolean idempotentReplay;

    /**
     * 处理后的申请状态
     */
    private String applyStatus;

    /**
     * 处理说明（幂等、回填失败原因等）
     */
    private String message;
}
