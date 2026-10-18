package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 合理用药审查命中项
 */
@Data
public class DrugRationalHitVO {

    /**
     * 命中类型（INTERACTION-相互作用 DOSE_SINGLE-单次超量 DOSE_DAILY-日累计超量）
     */
    private String hitType;

    /**
     * 严重度（1-禁忌 2-慎用；剂量类固定 2，只提示不拦）
     */
    private Integer severity;

    /**
     * 是否应当拦截审方通过（当前只有相互作用 1-禁忌为 true）
     */
    private Boolean blocked;

    /**
     * 命中的知识条目ID（便于回溯是哪条规则）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long knowledgeId;

    /** 成分关键字A */
    private String componentA;

    /** 成分关键字B */
    private String componentB;

    /** 甲方药品ID：处方里命中成分A的那条药品 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugIdA;

    /** 甲方药品名称 */
    private String drugNameA;

    /** 乙方药品ID：相互作用对侧命中的药品；剂量类命中为空 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugIdB;

    /** 乙方药品名称 */
    private String drugNameB;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 处理建议（剂量类命中为知识表的口径说明）
     */
    private String suggestion;
}
