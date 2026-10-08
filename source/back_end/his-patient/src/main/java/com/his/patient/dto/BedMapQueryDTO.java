package com.his.patient.dto;

import lombok.Data;

/**
 * 病区床位图查询入参
 */
@Data
public class BedMapQueryDTO {

    /** 科室ID：受限岗位只能传自己被授权的科室；不受限（data_scope=1）账号不传则落到主岗位科室 */
    private Long deptId;

    /** 病区ID：可选，只在上面已确定的科室内部再收一层 */
    private Long wardId;
}
