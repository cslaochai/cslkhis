package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 未对照院内项目（自动对照遍历用轻量行）。
 */
@Data
public class YbUnmappedItemVO {

    /**
     * 院内项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 院内项目编码
     */
    private String itemCode;

    /**
     * 院内项目名称
     */
    private String itemName;

    /**
     * 子类型（药品 drug_type 1西药2中成药3饮片；其余类型恒 0）
     */
    private Integer subType;
}
