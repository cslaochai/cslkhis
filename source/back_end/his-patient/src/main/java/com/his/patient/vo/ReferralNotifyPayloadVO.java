package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 转诊单登记通知总值班载荷（对应 {@code ReferralServiceImpl#notifyDutyOnCreate}）。
 *
 * <p>转诊是跨院动作，只有总值班能拍板联系与安排转运，所以登记即发一条待办；
 * payload 让收件箱不用点开就能看到转诊号与去向。
 */
@Data
public class ReferralNotifyPayloadVO implements Serializable {

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
     * 转诊事由
     */
    private String diagnosis;

    /**
     * 联系电话
     */
    private String contactPhone;
}
