package com.his.operation.vo;

import com.his.operation.entity.BizAnesthesiaPacu;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * PACU 复苏记录出参。
 *
 * <p>{@code aldreteTotal} 由服务端逐项相加写入，前端只做展示：
 * 允许前端改总分，出室标准就形同不存在。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PacuRecordVO extends BizAnesthesiaPacu {

    private String admissionNo;

    /**
     * 来源麻醉记录的状态 / 麻醉方式（判断 Aldrete 语境用）
     */
    private Integer recordStatus;

    private String recordStatusText;

    private Integer anesthesiaType;

    private String anesthesiaTypeText;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime anesthesiaEndTime;

    private String operationRoom;

    private String plannedOperationName;

    private String actualOperationName;

    // 文案
    /**
     * 状态文本
     */
    private String statusText;
    private String awarenessText;
    private String dispositionText;
    private String chargeStatusText;

    /**
     * 驻留时长（分钟，入室→出室；未出室则算到当前）
     */
    private Long stayMinutes;

    private String stayDurationText;

    /**
     * 计费用的驻留小时数（不足 1 小时按 1 小时）
     */
    private BigDecimal billHours;

    // 能力位
    /**
     * 在室：可登记 Aldrete 评分
     */
    private Boolean canScore;
    /**
     * 在室且已评分：可出室
     */
    private Boolean canLeave;
    /**
     * 已出室且未计费：可计费
     */
    private Boolean canCharge;

    /**
     * 是否满足出室标准（Aldrete ≥ 9）
     */
    private Boolean criteriaMet;

    private String warningText;
}
