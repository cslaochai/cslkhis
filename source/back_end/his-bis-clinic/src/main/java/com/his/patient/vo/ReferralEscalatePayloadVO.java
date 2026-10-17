package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 转诊待确认超时催办载荷（对应 ReferralServiceImpl#escalatePendingToDuty）。
 */
@Data
public class ReferralEscalatePayloadVO implements Serializable {

    /**
     * 转诊单号
     */
    private String referralNo;

    /**
     * 转诊方向文案（上转 / 下转）
     */
    private String direction;

    /**
     * 转入机构（为空即院内）
     */
    private String toHospital;

    /**
     * 已等待小时数
     */
    private Long waitedHours;
}
