package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全院床位池查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BedPoolQueryPageDTO extends PageParam {

    /** 科室ID（床位归属科室） */
    private Long deptId;

    /** 病区ID */
    private Long wardId;

    /** 床位状态（0-维修 1-空闲 2-占用 3-锁定） */
    private Integer bedStatus;

    /** 床位类型：normal / ICU / VIP */
    private String bedType;

    /** 床号或患者姓名模糊查询 */
    private String keyword;

    /** 只看空闲且未被预留的床（安排床位选床时用） */
    private Boolean availableOnly;
}
