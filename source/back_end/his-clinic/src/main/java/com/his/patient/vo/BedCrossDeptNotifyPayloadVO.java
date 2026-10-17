package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 跨科床位调配协调待办载荷（对应 BedCenterServiceImpl#notifyDutyCrossDept）。
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
