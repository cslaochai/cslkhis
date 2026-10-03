package com.his.operation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 日间手术统计维度项（术式名称 + 条数）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DaySurgeryItemCountVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /** 名称 */
    private String name;

    private Long count;
}
