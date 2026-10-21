package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 住院医嘱开立欠费管控校验结果。
 */
@Data
@NoArgsConstructor
public class ArrearsControlCheckResultVO implements Serializable {

    /**
     * 是否允许开单
     */
    private boolean allowed;

    /**
     * 拦截原因（allowed=false 时必填，可直接展示给医生）
     */
    private String reason;
}
