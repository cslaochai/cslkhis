package com.his.appoint.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 急诊交班接收提醒的站内信业务上下文（落 sys_message.payload）。
 */
@Data
public class EmergencyHandoverPayloadVO implements Serializable {

    /**
     * 交班单号
     */
    private String handoverNo;

    /**
     * 交班科室名称
     */
    private String deptName;

    /**
     * 交班人姓名
     */
    private String fromEmpName;

    /**
     * 本次移交的病人数
     */
    private Integer count;
}
