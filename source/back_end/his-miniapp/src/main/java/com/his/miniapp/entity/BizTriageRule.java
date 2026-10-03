package com.his.miniapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 智能导诊症状科室映射。
 *
 * <p>一行 = 「某个症状推荐某个科室」。命中逻辑全在代码里按 {@code keywords} 做关键词匹配，
 * 模型不参与决策 —— 它只负责把口语主诉归一成症状词、以及生成补充追问。
 * <b>急症信号（{@code urgent_flag}=1）必须是硬规则</b>：命中即置顶提示急诊，
 * 不能交给模型判断，否则「模型没看出来」不会有任何报错而患者已经在家等了一夜。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_triage_rule")
public class BizTriageRule extends BaseEntity {

    /** 症状编码 */
    private String symptomCode;

    /** 症状名称 */
    private String symptomName;

    /** 匹配关键词（顿号分隔） */
    private String keywords;

    /** 推荐科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 推荐科室名称（快照） */
    private String deptName;

    /** 推荐权重（越大越靠前） */
    private Integer weight;

    /** 急症信号（0-否 1-是） */
    private Integer urgentFlag;

    /** 就诊提示 */
    private String advice;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    /** 排序号 */
    private Integer sortOrder;
}
