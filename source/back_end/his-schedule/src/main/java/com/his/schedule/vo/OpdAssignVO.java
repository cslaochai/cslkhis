package com.his.schedule.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 门诊工位派岗记录出参。
 */
@Data
public class OpdAssignVO {

    /** 派岗记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 上岗日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate assignDate;

    /** 工位ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stationId;
    /** 工位名称快照 */
    private String stationName;
    /** 工位类别快照 */
    private Integer stationKind;
    /** 工位类别文案 */
    private String stationKindText;

    /** 上岗员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;
    /** 上岗员工姓名快照 */
    private String employeeName;
    /** 员工工号 */
    private String empCode;

    /** 上岗人岗位类别快照 */
    private Integer staffType;
    /** 岗位类别文案 */
    private String staffTypeName;

    /** 班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;
    /** 班次名称（联查） */
    private String shiftName;
    /** 班次开始 HH:mm */
    private String startTime;
    /** 班次结束 HH:mm */
    private String endTime;

    /** 是否跨单元借调（0否1是） */
    private Integer isLoan;
    /** 是否借调文案 */
    private String isLoanText;
    /** 借出方单元类型 */
    private Integer sourceOrgType;
    /** 借出方单元ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceOrgId;
    /** 借出方单元名称快照 */
    private String sourceOrgName;

    /** 派岗方式（1排班员指定） */
    private Integer assignMode;
    /** 状态（1在岗 0撤销） */
    private Integer status;
    /** 备注 */
    private String remark;
}
