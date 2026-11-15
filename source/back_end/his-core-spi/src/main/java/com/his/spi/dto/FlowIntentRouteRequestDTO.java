package com.his.spi.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 意图路由请求：一段自由文本 + 候选流程清单。
 */
@Data
public class FlowIntentRouteRequestDTO implements Serializable {

    /**
     * 患者/用户原话
     */
    private String text;

    /**
     * 入口渠道（MINIAPP/CONSOLE/...），仅用于审计
     */
    private String channel;

    /**
     * 候选流程，模型只能在其中选，不能自造编码
     */
    private List<FlowIntentOptionDTO> options;
}
