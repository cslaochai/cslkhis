package com.his.charge.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收费台首屏「待收费就诊」分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PendingEncounterQueryPageDTO extends PageParam {

    /**
     * 就诊类型（字典 his_encounter_type：1-门诊 2-住院）。
     * 收费台与住院账务是两条窗口，必须分开列，混在一起收费员分不清该收谁的钱。
     */
    @NotNull(message = "缺少就诊类型")
    private Integer encounterType;

    /**
     * 关键字：患者姓名 / 患者号 / 就诊单号模糊匹配
     */
    private String keyword;
}
