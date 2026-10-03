package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 科室实体
 */
@Data
public class SysDepartList {

    /** 科室ID */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptCode;
    /** 科室名称 */
    private String deptName;

    private Integer isPrimary;
}
