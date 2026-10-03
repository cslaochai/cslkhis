package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院医嘱模板明细（sql/103）。
 *
 * <p>无软删列：模板 upsert 时物理删旧重插（同 CSSD 器械包模板明细口径）。
 *
 * <p>单价只是录入当时的参考价 —— 套用时以后端字典现价覆盖。医嘱价是开立快照，
 * 模板若把过期旧价带进账单，四核对里「收费项目 ≠ 医嘱项目」会出现假阳性。
 * 同理，这里刻意<b>没有</b>开始时间与加急标志：那是当次临床决定，不该由模板固化。
 */
@Data
@TableName("biz_inpatient_order_template_item")
public class BizInpatientOrderTemplateItem implements Serializable {

    /** 明细ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 模板主表ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /**
     * 排序（服务端按提交顺序写，套用时保持医生录入顺序）
     */
    private Integer sortNo;

    /**
     * 医嘱类别：1-药品 2-检查 3-检验 4-治疗 5-护理 6-手术 7-输血 8-监护 9-其他 10-临床营养
     */
    private Integer orderClass;

    /**
     * 项目编码（药品/检查/检验字典码，套用时按它回查现价与下拉选中态）
     */
    private String itemCode;

    /** 项目名称 */
    private String itemName;

    /** 规格 */
    private String spec;

    /** 单位 */
    private String unit;

    /** 单次剂量 */
    private BigDecimal dosage;

    /** 剂量单位 */
    private String dosageUnit;

    /** 给药途径 */
    private String route;

    /** 频次 */
    private String frequency;

    /** 数量 */
    private BigDecimal quantity;

    /** 录入时单价 */
    private BigDecimal price;

    /** 创建时间 */
    private LocalDateTime createTime;
}
