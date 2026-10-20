package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合理用药审查命中项：相互作用或剂量上限比对出的一个问题。
 */
@Data
@NoArgsConstructor
public class DrugRationalCheckHitVO {

    /**
     * 命中类型（INTERACTION-相互作用 DOSE_SINGLE-单次超量 DOSE_DAILY-日累计超量）
     */
    private String hitType;

    /**
     * 是否应当拦截审方通过
     */
    private Boolean blocked;

    /**
     * 命中的知识条目ID（便于回溯是哪条规则）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long knowledgeId;

    /**
     * 甲方药品名称
     */
    private String drugNameA;

    /**
     * 乙方药品名称（相互作用对侧命中的药品；剂量类命中为空）
     */
    private String drugNameB;

    /**
     * 命中说明
     */
    private String message;

    /**
     * 处理建议
     */
    private String suggestion;
}
