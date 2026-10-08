package com.his.emr.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 发药入参
 */
@Data
public class DrugDispenseDTO {

    /**
     * 发药记录ID（单行发药时必填）
     */
    private Long id;

    /**
     * 处方ID（整单发药时必填）
     */
    private Long prescriptionId;

    /**
     * 药师ID（可选，缺省取当前登录员工ID）
     */
    private Long pharmacistId;

    /**
     * 药师姓名（可选，缺省取当前登录员工姓名）
     */
    private String pharmacistName;

    /**
     * 复核药师ID（麻醉药品、第一类精神药品**必填**）。
     * <p>
     * 只传 ID 不传姓名 —— 姓名由服务端按 ID 从员工表反查。
     * 复核是签名性质的动作，姓名可由前端随手给的话，复核记录就等于自述。
     * 复核人不得与发药人（当前登录员工）为同一人。
     */
    private Long checkerId;

    /**
     * 超限量理由。
     * <p>
     * 仅《处方管理办法》第24条允许的场景（第二类精神药品因慢性病等确需超 7 日用量的）
     * 可凭理由放行，理由写入专册备注。
     * 麻醉药品、第一类精神药品**没有**这个口子，传了也不放行。
     */
    private String overLimitReason;
}
