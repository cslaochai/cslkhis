package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 总值班临时换班入参。
 *
 * <p>换班<b>不改原值班人</b>：写 substitute_* 四列，解析时以换班后的人为准。
 * 排班表上"原本排的是谁"是追责依据，覆盖掉就等于把唯一证据删了。
 */
@Data
public class DutySubstituteDTO {

    @NotNull(message = "排班记录不能为空")
    private Long id;

    /** 换班后的实际值班人（必填且必须在职） */
    @NotNull(message = "换班后值班人不能为空")
    private Long substituteEmpId;

    /** 换班后的联系电话（留空回落新员工档案手机） */
    private String phone;

    /** 换班原因（必填：主班换人是全院协调的敏感动作，不能无理由改） */
    @NotBlank(message = "换班原因不能为空")
    private String substituteReason;
}
