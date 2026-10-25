package com.his.outpatient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 「门诊排班待我审批」查询入参：返回当前登录人作为门诊排班审核人的待审核排班。
 * 默认按分页返回；科室收口在 service 内按当前人门诊审核人身份现算，无需入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SchedulePendingQueryDTO extends PageParam {
}
