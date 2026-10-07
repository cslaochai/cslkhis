package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 等床超时催办载荷（对应 {@code BedCenterServiceImpl#escalateWaitToDuty}）。
 *
 * <p>与 {@code BedCrossDeptNotifyPayloadVO} 分开：那条是"床已排好、请确认接收"，
 * 这条是"还没排床、请协调或给处置意见"，读的人要一眼看出处于哪个阶段。
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
