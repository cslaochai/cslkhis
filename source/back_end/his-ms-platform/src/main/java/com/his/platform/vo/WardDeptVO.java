package com.his.platform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 病区收治科室出参
 */
@Data
public class WardDeptVO {

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 是否本位科室：1-是 0-否
     */
    private Integer isPrimary;

    /**
     * 专科优先床数（NULL=不限额）
     */
    private Integer bedQuota;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
