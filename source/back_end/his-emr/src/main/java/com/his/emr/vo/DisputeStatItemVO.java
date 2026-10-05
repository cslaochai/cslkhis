package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统计维度项（key=码值/科室ID，name=展示名，count=条数）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisputeStatItemVO implements Serializable {

    private String key;

    /**
     * 名称
     */
    private String name;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private Long count;
}
