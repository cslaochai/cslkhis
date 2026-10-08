package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 围手术期抗菌药物医嘱候选（给药证据，供点评人判定时机）
 */
@Data
public class IncisionDrugCandidateVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 药品名称 */
    private String drugName;

    /** 项目名称 */
    private String itemName;

    private Integer antibioticLevel;

    private String antibioticLevelText;

    private BigDecimal quantity;

    /** 单位 */
    private String unit;

    /** 开始时间 */
    private LocalDateTime startTime;

    private Integer orderStatus;

    /** 相对手术开始时间的分钟差：负数=术前给，正数=术后给（服务端算，前端不自己算） */
    private Long minutesFromIncision;
}
