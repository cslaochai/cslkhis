package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编码任务分配入参（病案室指定编码员）
 */
@Data
public class CodeTaskAssignUpsertDTO {

    /** 任务ID */
    @NotNull(message = "任务ID不能为空")
    private Long id;

    /** 编码人员工ID（姓名服务端按员工反查，不信任前端） */
    @NotNull(message = "请选择编码员")
    private Long coderId;
}
