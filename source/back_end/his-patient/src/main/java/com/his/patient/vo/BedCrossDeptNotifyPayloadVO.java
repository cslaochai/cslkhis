package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 跨科床位调配协调待办载荷（对应 {@code BedCenterServiceImpl#notifyDutyCrossDept}）。
 *
 * <p>bizId 用调配单而不是等床单，等床超时催办也走 duty-coord，两条事件共用一个 bizId 会被判重互相吃掉。
 */
@Data
public class BedCrossDeptNotifyPayloadVO implements Serializable {

    /**
     * 等床单号
     */
    private String waitNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 调配单号
     */
    private String allocateNo;

    /**
     * 床位所属科室名称
     */
    private String ownDeptName;

    /**
     * 用床科室名称（申请科室）
     */
    private String useDeptName;

    /**
     * 床号
     */
    private String bedNo;
}
