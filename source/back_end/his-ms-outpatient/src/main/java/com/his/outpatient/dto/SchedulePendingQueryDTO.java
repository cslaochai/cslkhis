package com.his.outpatient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 「门诊排班待我审批」查询入参：返回当前登录人作为门诊排班审核人的待审核排班。
 * 默认按分页返回；科室收口在 service 内按当前人门诊审核人身份现算，无需入参。
 * 以下字段均为可选筛选条件（前端查询卡下拉传入），不传则不过滤。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SchedulePendingQueryDTO extends PageParam {
    /**
     * 门诊科室 ID（精确匹配，可选）
     */
    private Long deptId;

    /**
     * 医生 ID（精确匹配，可选）
     */
    private Long doctorId;

    /**
     * 排班日期起（含，可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDateStart;

    /**
     * 排班日期止（含，可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDateEnd;
}
