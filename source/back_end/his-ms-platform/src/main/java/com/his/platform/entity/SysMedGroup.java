package com.his.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医疗组 / 主诊组：科室下的医师分组。
 *
 * <p>依据《三级医院评审标准(2022年版)实施细则》2.2.27——科主任根据科室/病区床位、工作量、
 * 医师专业资质组建若干医疗团队（主诊医师制 / 医疗组长制），指定负责人，实行三级查房。
 *
 * <p>挂载位置是**科室**，不是病区：医生归属固定在科室与医疗组，查房时到病区，
 * 跨科收治时「医生跟着患者走」仍回原科室主诊医师负责。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_med_group")
public class SysMedGroup extends BaseEntity {

    /**
     * 医疗组编码（唯一）
     */
    private String groupCode;

    /**
     * 医疗组名称（如「呼吸内科一组」）
     */
    private String groupName;

    /**
     * 所属科室ID（sys_department.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 主诊医师 / 医疗组长（sys_employee.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long leaderId;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
