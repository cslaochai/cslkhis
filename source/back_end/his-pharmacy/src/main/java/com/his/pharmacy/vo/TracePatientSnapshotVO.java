package com.his.pharmacy.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 患者姓名/号快照（his-patient 的 biz_patient，跨模块裸 SQL 只读两列）。
 */
@Data
public class TracePatientSnapshotVO implements Serializable {

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;
}
