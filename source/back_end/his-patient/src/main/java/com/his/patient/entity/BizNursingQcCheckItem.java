package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 护理质量检查单明细（护理质量检查明细，sql/168）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nursing_qc_check_item")
public class BizNursingQcCheckItem extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 检查单ID
     */
    private Long checkId;
    /**
     * 检查项ID
     */
    private Long itemId;
    /**
     * 项目编码
     */
    private String itemCode;
    /**
     * 项目名称
     */
    private String itemName;
    /**
     * 类别快照：按类别聚合台账指标时不用再回 JOIN 标准目录
     */
    private Integer category;
    /**
     * 抽查例数
     */
    private Integer checkedNum;
    /**
     * 合格例数，不得大于 {@code checkedNum}（服务层校验）
     */
    private Integer qualifiedNum;
    /**
     * 本项应得分
     */
    private BigDecimal fullScore;
    /**
     * 实得分 = 应得分 × 合格/抽查，由服务端算，不接受前端传
     */
    private BigDecimal score;
    /**
     * 存在问题
     */
    private String problem;
    /**
     * 原因分析
     */
    private String causeAnalysis;
    /**
     * 整改措施
     */
    private String rectifyMeasure;
}
