package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 护理质量检查单明细（护理质量检查明细，sql/168）。
 *
 * <p>一行 = 一张检查单里的一个检查项：抽查例数、合格例数、应得分、实得分，
 * 外加 PDCA 的后两环 —— 存在问题 / 原因分析 / 整改措施。
 * 只记「合格与否」不记原因，检查表就退化成打分表，护理部月度通报里写不出「为什么跌了」。
 *
 * <p>项目名与应得分<b>是快照</b>（项目名称/{@code full_score}）：
 * 标准目录护理质控检查项目录会随评审细则改版调整分值，改版不能把去年的检查单重算成新的分数。
 *
 * <p>{@code uk_check_item(check_id, item_id)} 不含 del_flag ⇒ <b>删除走物理删</b>：
 * 整单重存是「先清明细再插明细」，软删留下的行还占着键，第二步必然 Duplicate entry。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nursing_qc_check_item")
public class BizNursingQcCheckItem extends BaseEntity {

    /** 检查单ID */
    private Long checkId;
    /** 检查项ID */
    private Long itemId;
    /** 项目编码（快照） */
    private String itemCode;
    /** 项目名称 */
    private String itemName;
    /** 类别快照：按类别聚合台账指标时不用再回 JOIN 标准目录 */
    private Integer category;
    /** 抽查例数 */
    private Integer checkedNum;
    /** 合格例数，不得大于 {@code checkedNum}（服务层校验） */
    private Integer qualifiedNum;
    /** 本项应得分（快照） */
    private BigDecimal fullScore;
    /** 实得分 = 应得分 × 合格/抽查，由服务端算，不接受前端传 */
    private BigDecimal score;
    /** 存在问题 */
    private String problem;
    /** 原因分析 */
    private String causeAnalysis;
    /** 整改措施 */
    private String rectifyMeasure;
}
