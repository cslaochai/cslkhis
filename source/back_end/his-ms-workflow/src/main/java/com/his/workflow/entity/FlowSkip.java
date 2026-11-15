package com.his.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 流转线（sys_flow_skip）：从 nowNodeCode 指向 nextNodeCode。
 */
@Data
@TableName("sys_flow_skip")
public class FlowSkip {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long definitionId;

    private String nowNodeCode;

    private String nextNodeCode;

    /** 流转线类型（PASS-正常 COND-条件 REJECT-驳回），见 FlowSkipTypeEnum */
    private String skipType;

    /**
     * 条件表达式，仅 COND 使用。语法：变量名 运算符 字面量，如 route.flowCode == 'REPORT'。
     * 关键字 DEFAULT = 兜底分支（条件分支都没命中时走它）。
     */
    private String conditionExpr;

    /** 同层多条条件线的匹配顺序，升序 */
    private Integer sortNo;
}
