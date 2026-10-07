package com.his.appoint.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 急诊候诊超时催办的站内信业务上下文（落 {@code sys_message.payload}）。
 *
 * <p><b>字段名是前后端契约，不能随手改</b>：消息中心点开这条催办时按这些字段渲染
 * 摘要 chip，改名 = 少显示一个字段，而且不会报错。
 *
 * <p>曾用 {@code LinkedHashMap<String, Object>} 拼这个 JSON —— 键名是隐式契约：
 * 拼错不报错、改字段名时前端静默少渲染一块。
 */
@Data
public class EmergencyWaitTodoPayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 急诊单号
     */
    private String emergencyNo;

    /**
     * 分级中文名
     */
    private String triageLevelText;

    /**
     * 急诊科名称
     */
    private String deptName;

    /**
     * 已候诊分钟数
     */
    private Long waitMinutes;

    /**
     * 该分级应就诊的目标时限（分钟）
     */
    private Integer targetSeeMinutes;
}
