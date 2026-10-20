package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 员工主数据摘要：跨模块取姓名快照、在职状态与排班要用的执业标记。
 */
@Data
@NoArgsConstructor
public class EmployeeBriefVO {

    /**
     * 员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 员工编号
     */
    private String empCode;

    /**
     * 员工姓名
     */
    private String empName;

    /**
     * 员工类型（1-医生 2-护士 3-收费员 4-药剂师 5-管理员 6-其他）
     */
    private Integer empType;

    /**
     * 主岗位科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 主岗位科室名称
     */
    private String deptName;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 职称
     */
    private String title;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 是否本院固定医师（0-轮转 1-固定 NULL-未登记）
     */
    private Integer isFixed;

    /**
     * 本机构注册执业（0-否 1-是 NULL-未登记）
     */
    private Integer isRegistered;
}
