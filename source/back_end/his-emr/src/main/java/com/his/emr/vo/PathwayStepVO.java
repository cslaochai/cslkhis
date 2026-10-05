package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 临床路径步骤 VO（路径日 × 诊疗项目，文书记录）。
 */
@Data
public class PathwayStepVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pathwayId;

    /**
     * 路径日（入院当天=1）
     */
    private Integer dayNo;

    /**
     * 项目类型（1-诊疗 2-用药 3-手术操作 4-护理 5-病情评估 6-宣教）
     */
    private Integer itemType;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 字典项目编码（可空；医生站偏离比对用）
     */
    private String itemCode;

    /**
     * 路径要求/具体内容
     */
    private String content;

    /**
     * 同日内的顺序
     */
    private Integer sortNo;
}
