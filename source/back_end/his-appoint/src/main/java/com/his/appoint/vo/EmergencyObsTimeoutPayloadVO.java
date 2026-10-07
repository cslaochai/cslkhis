package com.his.appoint.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 急诊留观超时限催办的站内信业务上下文（落 {@code sys_message.payload}）。
 *
 * <p><b>字段名是前后端契约，不能随手改</b>：被催的医生点开这条提醒时要看到
 * 「哪个人、留观了几小时、占的是哪张床」，改名 = 消息中心少显示一块信息。
 *
 * <p>曾用 {@code LinkedHashMap<String, Object>} 拼这个 JSON —— 键名是隐式契约，
 * 拼错不报错。
 */
@Data
public class EmergencyObsTimeoutPayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 急诊单号
     */
    private String emergencyNo;

    /**
     * 急诊科名称
     */
    private String deptName;

    /**
     * 已留观小时数
     */
    private Long obsHours;

    /**
     * 占用的留观床号（未登记时为 null）
     */
    private String observationBed;
}
