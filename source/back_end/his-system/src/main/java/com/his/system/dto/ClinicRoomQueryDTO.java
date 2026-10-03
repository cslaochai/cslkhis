package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 诊室查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClinicRoomQueryDTO extends PageParam {

    /**
     * 诊室名称，模糊匹配
     */
    private String name;

    /**
     * 所属科室ID，关联科室表
     */
    private Long deptId;

    /** 诊室状态（1-启用 0-停用） */
    private Integer status;
}
