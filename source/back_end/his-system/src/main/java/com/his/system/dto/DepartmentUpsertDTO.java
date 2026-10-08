package com.his.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 科室新增/修改入参
 */
@Data
public class DepartmentUpsertDTO {

    /**
     * 科室ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 科室编码，系统自动生成（新增时可不传）
     */
    private String deptCode;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他）
     */
    private Integer deptType;

    /**
     * 上级科室ID，顶级科室为0
     */
    private Long parentId;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 科室图标
     */
    private String deptIcon;

    /**
     * 科室描述
     */
    private String deptDesc;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 地理位置/地址
     */
    private String location;

    /**
     * 是否开放：0-否 1-是
     */
    private Integer isOpen;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
}
