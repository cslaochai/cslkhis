package com.his.platform.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
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
     * 科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔
     */
    private String deptType;

    /**
     * 节点类型（0-医疗机构 1-管理片区 2-临床科室 3-医技科室 4-药剂科室 5-行政后勤 6-医疗辅助 7-其他）
     * <p>必填且单值：决定这个节点能不能被当成排班单元/收治科室，漏填等于让片区混进科室。
     */
    @NotNull(message = "节点类型不能为空")
    @AllowedValues(values = {"0", "1", "2", "3", "4", "5", "6", "7"}, message = "节点类型取值不合法")
    private String nodeType;

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
     * 科室负责人ID（sys_employee.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptLeaderId;

    /**
     * 门诊排班审核人ID（sys_employee.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long outpatientAuditorId;

    /**
     * 护理排班审核人ID（sys_employee.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nursingAuditorId;

    /**
     * 全院岗位排班审核人ID（sys_employee.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleAuditorId;

    /**
     * 是否开放：0-否 1-是
     */
    private Integer isOpen;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
}
