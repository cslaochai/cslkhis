package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 患者标签摘要：跨模块把标签ID/名称翻译成展示字段的出参。
 */
@Data
@NoArgsConstructor
public class PatientTagBriefVO {

    /**
     * 标签ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签简称
     */
    private String shortName;

    /**
     * 标签颜色
     */
    private String tagColor;
}
