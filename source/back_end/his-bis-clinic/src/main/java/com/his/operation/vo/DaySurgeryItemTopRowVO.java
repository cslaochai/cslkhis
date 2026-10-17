package com.his.operation.vo;

import lombok.Data;

import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 日间手术术式分布 TOP10 的一行（BizDaySurgeryApplyMapper#countByItemTop 的返回行）。
 */
@Data
public class DaySurgeryItemTopRowVO implements Serializable {

    /**
     * 术式ID（可能为 NULL —— 历史单据没录术式）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 术式名称快照（缺失时为「未知术式」）
     */
    private String itemName;

    /**
     * 该术式的登记单数
     */
    private Long cnt;
}
