package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 检查预约站内信的业务上下文载荷（消息中心点开提醒时按这些字段渲染摘要 chip）。
 *
 * <p><b>字段名是前后端契约，不能随手改</b>：前端 {@code src/lib/messageCatalog.js} 的
 * {@code PAYLOAD_KEYS} 白名单按 {@code patientName / itemName} 这些键渲染 chip，
 * 改名 = 消息中心少显示一个字段。
 *
 * <p>曾用 {@code LinkedHashMap<String, Object>} 拼这个 JSON —— 键名是隐式契约：
 * 拼错不报错、改字段名时前端静默少渲染一个 chip。
 */
@Data
public class ExamApptMessagePayloadVO implements Serializable {

    /**
     * 预约单号
     */
    private String apptNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 检查项目名称
     */
    private String itemName;

    /**
     * 设备名称
     */
    private String deviceName;
}