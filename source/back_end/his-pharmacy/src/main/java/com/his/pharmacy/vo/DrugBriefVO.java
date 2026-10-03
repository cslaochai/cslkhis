package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 药品简要信息（生成入库单时从药品字典取快照用）
 */
@Data
public class DrugBriefVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 药品编码（快照） */
    private String drugCode;

    /** 药品名称（快照） */
    private String drugName;

    /** 规格（快照） */
    private String specification;

    /** 单位 */
    private String unit;
}
