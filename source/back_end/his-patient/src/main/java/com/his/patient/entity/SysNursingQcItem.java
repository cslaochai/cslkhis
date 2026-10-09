package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 护理质控检查项标准目录（护理质控检查项目录，sql/168）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_nursing_qc_item")
public class SysNursingQcItem extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 项目编码（BN/SC/SF/DC/IP + 两位序号）
     */
    private String itemCode;
    /**
     * 检查项目名称
     */
    private String itemName;
    /**
     * NursingQcCategoryEnum：1-基础护理 2-专科护理 3-安全管理 4-护理文书 5-院感防控
     */
    private Integer category;
    /**
     * 计入的台账指标编码（NursingIndicatorEnum），NULL=只进检查表不出指标
     */
    private String indicatorCode;
    /**
     * 评价标准
     */
    private String standard;
    /**
     * 本项应得分
     */
    private BigDecimal fullScore;
    /**
     * 单项目标合格率（%）
     */
    private BigDecimal targetRate;
    /**
     * 1-护理部每轮必查的重点项
     */
    private Integer keyFlag;
    /**
     * 同类别内排序
     */
    private Integer sortOrder;
    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
