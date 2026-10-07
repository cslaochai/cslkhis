package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 纠纷/投诉：被投诉科室 TOP（count 倒序前 10）。
 */
@Data
public class DisputeDeptCountVO implements Serializable {

    /**
     * 科室ID（无科室时 SQL 给 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long d;

    /**
     * 科室名（无科室时给「未指定科室」）
     */
    private String n;

    /**
     * 条数
     */
    private Long c;
}