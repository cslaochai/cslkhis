package com.his.spi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 意图路由结果：模型从候选中选出的流程编码。
 */
@Data
public class FlowIntentRouteResultVO implements Serializable {

    /**
     * 选中的流程编码；模型无法判断时为空
     */
    private String flowCode;

    /**
     * 置信度 0~1，低于阈值时调用方应走默认分支
     */
    private Double confidence;

    /**
     * 判定理由，一句话；落实例变量便于复盘
     */
    private String reason;

    private boolean degraded;

    private String degradeReason;
}
