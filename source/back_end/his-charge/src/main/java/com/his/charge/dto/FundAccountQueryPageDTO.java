package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资金账户分页查询入参（L3 台账）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FundAccountQueryPageDTO extends PageParam {

    /**
     * 关键字：患者姓名 / 患者号模糊匹配
     */
    private String keyword;

    /**
     * 账户主体类型（字典 his_account_owner_type：1-患者（门诊余额） 2-住院就诊次（预交金））
     */
    private Integer ownerType;

    /**
     * 账户状态（字典 his_account_status：1-正常 0-冻结）
     */
    private Integer accountStatus;

    /**
     * 患者ID（患者详情页联动）
     */
    private Long patientId;
}
