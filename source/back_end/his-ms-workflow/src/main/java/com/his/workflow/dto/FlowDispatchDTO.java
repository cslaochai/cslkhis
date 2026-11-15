package com.his.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一入口分发：一段自由文本先过 AI 意图识别，再启动对应流程。
 */
@Data
public class FlowDispatchDTO implements Serializable {

    @NotBlank(message = "文本内容不能为空")
    private String text;

    /** 入口渠道（MINIAPP/CONSOLE/...），审计用 */
    private String channel;

    /** 业务类型，审计与检索用 */
    private String bizType;

    /** 业务单据 ID，可空 */
    private String businessId;

    /** 直接指定流程编码（跳过意图识别，运维排障与内部调用用） */
    private String flowCode;

    /** 入口变量 JSON */
    private String variableJson;

    private Long startedByUserId;

    private String startedByName;
}
