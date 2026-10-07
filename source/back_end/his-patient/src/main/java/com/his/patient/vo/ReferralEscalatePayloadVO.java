package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 转诊待确认超时催办载荷（对应 {@code ReferralServiceImpl#escalatePendingToDuty}）。
 *
 * <p>与 {@code ReferralNotifyPayloadVO} 分开建类而不是共用：登记通知关心"去向与事由"，
 * 超时催办关心"挂了多久"，字段集合本就不同，塞一个类里必然有一半字段长期为 null。
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
