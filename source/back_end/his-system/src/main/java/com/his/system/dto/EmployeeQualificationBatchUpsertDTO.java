package com.his.system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 员工资格证书批量保存入参（档案页一次提交多本证书）。
 * 展开、查重、落库全部在后端单事务内完成，前端禁止逐条调单本证书接口。
 */
@Data
public class EmployeeQualificationBatchUpsertDTO {

    /**
     * 员工ID：本批证书统一挂这个人，条目里再传别的也以这里为准（后端逐条覆盖归属）
     */
    @NotNull(message = "员工不能为空")
    private Long employeeId;

    /**
     * 证书条目（新增=无 id，修改=带 id）
     */
    @Valid
    @NotEmpty(message = "证书不能为空")
    @Size(max = 50, message = "一次最多保存 50 本证书")
    private List<EmployeeQualificationUpsertDTO> items;
}
