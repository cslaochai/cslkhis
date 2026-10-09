package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 药物相互作用知识条目
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_drug_interaction")
public class SysDrugInteraction extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 成分关键字A（书写顺序不承载语义，唯一性靠 pairKey）
     */
    private String componentA;

    /**
     * 成分关键字B
     */
    private String componentB;

    /**
     * 成分对归一化键（两关键字按二进制序排序后以 & 连接，服务端生成，不由前端传）
     */
    private String pairKey;

    /**
     * 严重度（1-禁忌：审方通过被拦 2-慎用：只标注不拦）
     */
    private Integer severity;

    /**
     * 相互作用后果（审方提示正文，退回理由逐字引用此列）
     */
    private String interactionDesc;

    /**
     * 处理建议（换药/减量/监测什么指标）
     */
    private String suggestion;

    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;
}
