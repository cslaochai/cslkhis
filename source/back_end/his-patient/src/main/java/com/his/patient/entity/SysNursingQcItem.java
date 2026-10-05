package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 护理质控检查项标准目录（护理质控检查项目录，sql/168）。
 *
 * <p>全院一份的打分依据：26 项分 5 个类别，<b>每个类别的应得分合计恒为 100</b>
 * （自检 T2 盯这条），这样「实得分/应得分」在任何类别都是得分率，不用换算。
 *
 * <p>只有类别 1（基础护理）与类别 4（护理文书）的 {@code indicator_code} 有值 ——
 * 这两类进月度台账出合格率指标；专科/安全/院感三类只进检查表。
 * 检查项是否「合格」由现场检查人按 {@code standard} 判，所以标准文字要写到能判的程度
 * （「按护理级别巡视并在记录单留痕，巡视间隔不超限」，不是「巡视到位」）。
 *
 * <p>{@code uk_qc_item_code(item_code)} 不含 del_flag ⇒ <b>删除走物理删</b>（AGENTS §3）：
 * 停用一项就置 status=0，老检查单靠自己的明细快照照常渲染。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_nursing_qc_item")
public class SysNursingQcItem extends BaseEntity {

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
