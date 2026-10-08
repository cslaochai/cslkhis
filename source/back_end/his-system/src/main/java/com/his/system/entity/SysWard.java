package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 病区主数据（只读参照：排班要把病区名称与所属科室落成快照，不参与写入）。
 */
@Data
@TableName("sys_ward")
public class SysWard {

    @TableId(value = "ward_id", type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区编码
     */
    private String wardCode;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 所属科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 状态（0-停用 1-正常）
     */
    private Integer status;
}
