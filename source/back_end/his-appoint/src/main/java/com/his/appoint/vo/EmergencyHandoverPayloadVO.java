package com.his.appoint.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 急诊交班接收提醒的站内信业务上下文（落 {@code sys_message.payload}）。
 *
 * <p><b>字段名是前后端契约，不能随手改</b>：接续医生点开这条提醒时要看到
 * 「谁交给谁、几个病人、哪个科」，改名 = 消息中心少显示一块信息。
 *
 * <p>曾用 {@code LinkedHashMap<String, Object>} 拼这个 JSON —— 键名是隐式契约，
 * 拼错不报错。
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
