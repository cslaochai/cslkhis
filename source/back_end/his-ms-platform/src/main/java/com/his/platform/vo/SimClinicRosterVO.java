package com.his.platform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 仿真实验室门诊花名册（只读）：科室 → 出诊医生/诊室 → 分诊护士账号。
 *
 * <p>供 cssimlab 启动时拉取真实岗位名册建会话，不再在仿真侧维护一份平行账号矩阵。
 * 一行 = 一个科室的一个出诊单元；triageUsername 按科室挂在该行上。
 */
@Data
public class SimClinicRosterVO {

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称 */
    private String deptName;

    /** 本科室分诊护士登录账号（10020） */
    private String triageUsername;

    /** 出诊医生员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 出诊医生姓名 */
    private String doctorName;

    /** 出诊医生登录账号 */
    private String username;

    /** 诊室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    /** 诊室名称 */
    private String roomName;
}
