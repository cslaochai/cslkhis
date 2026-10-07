package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 治疗项目快照（项目本身 + 能解析出来的执行科室名）。
 *
 * <p>科室名一律 LEFT JOIN 科室现算：老字典里科室ID 只有 101/105 两个值且在科室表中
 * **不存在**（孤儿引用），直接快照字典里的科室会写进一个根本不存在的科室。
 * 查不到就返回 null，页面显示「未指定」，不兜底。
 *
 * <p>跨模块只读一张字典表、不引 his-system 的实体依赖。
 */
@Data
public class TreatmentItemSnapshotVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    private String itemCode;

    private String itemName;

    /**
     * 项目类型
     */
    private Integer itemType;

    private BigDecimal price;

    /**
     * 疗程时长（分钟/天，按项目类型含义不同）
     */
    private Integer duration;

    private String usageMethod;

    /**
     * 启用状态（1-启用 0-停用）
     */
    private Integer status;

    /**
     * 执行科室ID（字典里的孤儿引用可能查不到科室名）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long execDeptId;

    private String execDeptName;
}