package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 门诊工位派岗新增/修改入参（sql/277，排班员指定）。
 */
@Data
public class OpdAssignUpsertDTO {

    /** 主键ID（空=新增派岗） */
    private Long id;

    /** 上岗日期 */
    @NotNull(message = "上岗日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate assignDate;

    /** 工位ID */
    @NotNull(message = "请选择工位")
    private Long stationId;

    /** 上岗员工ID */
    @NotNull(message = "请选择上岗人员")
    private Long employeeId;

    /** 班次ID（可空，工位默认班次可兜底） */
    private Long shiftId;

    /** 是否跨单元借调（0-否 1-是，空=否） */
    @AllowedValues(values = {"0", "1"}, message = "是否借调取值不合法")
    private Integer isLoan;

    /** 借出方单元类型（借调填：1科室 2病区 3全院） */
    @AllowedValues(values = {"1", "2", "3"}, message = "借出方单元类型取值不合法")
    private Integer sourceOrgType;

    /** 借出方单元ID（借调填，可空；可只填名称） */
    private Long sourceOrgId;

    /** 借出方单元名称（借调填；优先用它，空了再按 orgId 查） */
    private String sourceOrgName;

    /** 备注 */
    private String remark;
}
