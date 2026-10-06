package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 费用记账行分页查询入参（L1 台账）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FeeRecordQueryPageDTO extends PageParam {

    /**
     * 关键字：记账流水号 / 项目名称 / 患者姓名 / 来源单号模糊匹配
     */
    private String keyword;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 就诊类型（字典 his_encounter_type：1-门诊 2-住院）
     */
    private Integer encounterType;

    /**
     * 就诊标识
     */
    private Long encounterId;

    /**
     * 记账状态（字典 his_fee_status：1-待结算 2-已锁定 3-已结算 4-已红冲）
     */
    private Integer feeStatus;

    /**
     * 项目类型（1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗 8-耗材）
     */
    private Integer itemType;

    /**
     * 费用来源
     */
    private Integer sourceType;

    /**
     * 费用归属科室
     */
    private Long deptId;

    /**
     * 归属账单ID：查"这张账单由哪些费用组成"
     */
    private Long billId;
}
