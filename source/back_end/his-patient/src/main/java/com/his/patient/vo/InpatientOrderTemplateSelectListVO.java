package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 医嘱模板下拉候选（开立弹窗顶部「套用模板」用，命名遵循 AGENTS.md：下拉出参用 `xxxSelectListVO`）。
 */
@Data
public class InpatientOrderTemplateSelectListVO implements Serializable {

    /**
     * 模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 默认医嘱类型（1-长期 2-临时）
     */
    private Integer orderType;

    private String orderTypeText;

    /**
     * 明细条数
     */
    private Integer itemCount;
}
