package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 欠费提醒站内信的报文载荷（{@code InpatientAccountServiceImpl#notifyArrears}）。
 *
 * <p>这三个金额<b>刻意序列化成字符串</b>（{@code toPlainString()}）而不是数字：
 * 站内信是要落库留档、也可能被第三方消息推送转发的，字符串形态不会被下游的
 * JSON 解析器重新解释成浮点，欠多少钱这个事实在任何一环都不会变形。
 *
 * <p>⚠ 键名是对外契约（前端消息中心与推送模板按key 取值），不得改动。
 */
@Data
public class ArrearsNoticePayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 欠费金额（元，字符串形态）
     */
    private String arrears;

    /**
     * 住院账户余额（元，字符串形态）
     */
    private String balance;

    /**
     * 触发场景（如"入院登记" / "出院结算"）
     */
    private String scene;
}