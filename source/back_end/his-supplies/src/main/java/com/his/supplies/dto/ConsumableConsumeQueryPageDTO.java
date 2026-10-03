package com.his.supplies.dto;

import com.his.common.base.PageParam;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材领用台账分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumableConsumeQueryPageDTO extends PageParam {
    /** 关键字（耗材名/领用单号模糊） */
    private String keyword;
    /** 领用科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
}
