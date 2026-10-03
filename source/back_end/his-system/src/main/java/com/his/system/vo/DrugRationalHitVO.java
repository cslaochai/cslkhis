package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 合理用药审查命中项
 * <p>
 * 一条命中就是「这两张单据行之间」或「这一张单据行自己」的问题。提示正文分两种来源：
 * 相互作用<b>逐字引用知识表的 interaction_desc</b>（药师看到的每个字都可追溯到规则来源，
 * 被质疑时能回答"凭什么拦我"）；剂量上限的正文是服务端现算的比对结果（知识表只存阈值不存结论）。
 */
@Data
public class DrugRationalHitVO {

    /** 命中类型（INTERACTION-相互作用 DOSE_SINGLE-单次超量 DOSE_DAILY-日累计超量） */
    private String hitType;

    /** 严重度（1-禁忌 2-慎用；剂量类固定 2，只提示不拦） */
    private Integer severity;

    /** 是否应当拦截审方通过（当前只有相互作用 1-禁忌为 true） */
    private Boolean blocked;

    /** 命中的知识条目ID（便于回溯是哪条规则） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long knowledgeId;

    private String componentA;

    private String componentB;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugIdA;

    private String drugNameA;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugIdB;

    private String drugNameB;

    /** 消息内容 */
    private String message;

    /** 处理建议（剂量类命中为知识表的口径说明） */
    private String suggestion;
}
