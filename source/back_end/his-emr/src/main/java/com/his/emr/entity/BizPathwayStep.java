package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 临床路径步骤。
 *
 * <p>路径日 × 诊疗项目：「该做什么」的文书记录，不生成医嘱、不计费（避免与医嘱链双轨）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pathway_step")
public class BizPathwayStep extends BaseEntity implements Serializable {

    /** 模板ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pathwayId;

    /** 路径日（入院当天=1，封顶 total_days） */
    private Integer dayNo;

    /** 项目类型:1-诊疗 2-用药 3-手术操作 4-护理 5-病情评估 6-宣教（字典 his_pathway_item_type） */
    private Integer itemType;

    /** 项目名称 */
    private String itemName;

    /** 字典项目编码（治疗项目/药品，供医嘱变异比对；自由文本步骤为 NULL） */
    private String itemCode;

    /** 路径要求/具体内容 */
    private String content;

    /** 同日内的顺序 */
    private Integer sortNo;
}
