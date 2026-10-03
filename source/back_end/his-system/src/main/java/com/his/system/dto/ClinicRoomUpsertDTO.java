package com.his.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 诊室新增/修改入参
 */
@Data
public class ClinicRoomUpsertDTO {

    /**
     * 诊室ID，新增时为空，修改时必填
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 诊室名称，如：心内科一诊室、302诊室
     */
    private String name;

    /**
     * 诊室编号（ABCD）
     */
    private String code;

    /**
     * 地理位置，如：门诊楼A座3层
     */
    private String location;

    /**
     * 所属科室ID，关联科室表
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 诊室状态（1-启用 0-停用） */
    private Integer status;
}
