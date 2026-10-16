package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 等床超时催办载荷（对应 BedCenterServiceImpl#escalateWaitToDuty）。
 */
@Data
public class BedWaitEscalatePayloadVO implements Serializable {

    /**
     * 等床单号
     */
    private String waitNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 申请科室名称
     */
    private String applyDeptName;

    /**
     * 已等待小时数
     */
    private Long waitedHours;
}
