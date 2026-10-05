package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 员工岗位VO：一个人在一个科室以某个角色执业。
 *
 * <p>顶栏「切换岗位」与角色/科室的授权集合都出自这一份，不再分别取角色列表和科室列表 ——
 * 分开的两份数据能被拼成「医生 · 药房」这种现实中不存在的组合。
 */
@Data
public class EmployeePostVO {

    /**
     * 角色ID（角色的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;

    /**
     * 角色编码（角色.role_code），切换时回传给 /auth/switchPost
     */
    private String roleCode;

    private String roleName;

    /**
     * 科室ID（科室的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptCode;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 是否主岗位（= 该员工的主科室，一人只有一条）：0-否 1-是
     */
    private Integer isPrimary;

    /**
     * 岗位生效日期（NULL=保存即生效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate effectiveDate;

    /**
     * 岗位失效日期（NULL=长期有效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate expireDate;

    /**
     * 岗位状态（派生值，不落列）：1-在职 2-已失效。
     * 到期与否恒等于 expireDate 与今天的比较，存列只会多出一个会漂移的口径。
     */
    private Integer postStatus;
}
