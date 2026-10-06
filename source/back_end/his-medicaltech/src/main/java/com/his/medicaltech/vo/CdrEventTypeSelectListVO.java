package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 事件类型字典项（前端做筛选下拉与"这类事件归在哪个节点下"的归类）。
 */
@Data
@Schema(description = "患者全景事件类型字典项")
public class CdrEventTypeSelectListVO implements Serializable {

    @Schema(description = "事件类型码")
    private String code;

    @Schema(description = "事件类型名称")
    private String text;

    @Schema(description = "归属的节点类型码")
    private String nodeType;

    @Schema(description = "归属的节点类型名称")
    private String nodeTypeText;

    @Schema(description = "副码的中文标签，为空表示这类事件没有副码")
    private String secondaryLabel;
}
