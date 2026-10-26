package com.his.outpatient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 听班呼叫流水视图（sql/297）。
 */
@Data
public class DutyCallLogVO {

    /** 流水ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 呼叫单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long callId;

    /** 呼叫单号 */
    private String callNo;

    /** 动作：1-呼叫 2-到岗 3-超时升级 4-取消 5-通知失败 */
    private Integer actionType;

    /** 动作文案 */
    private String actionTypeText;

    /** 动作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actionTime;

    /** 动作前状态 */
    private Integer fromStatus;

    /** 动作前状态文案 */
    private String fromStatusText;

    /** 动作后状态 */
    private Integer toStatus;

    /** 动作后状态文案 */
    private String toStatusText;

    /** 操作人ID（系统触发为空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /** 操作人姓名（系统触发为空） */
    private String operatorName;

    /** 动作说明 */
    private String actionNote;
}
