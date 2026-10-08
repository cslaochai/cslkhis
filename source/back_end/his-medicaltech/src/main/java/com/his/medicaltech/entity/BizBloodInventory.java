package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 血库血袋库存台账（血库血袋库存）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_blood_inventory")
public class BizBloodInventory extends BaseEntity {

    /**
     * 血袋号（全局唯一）
     */
    private String bagNo;

    /**
     * 血型（1-A 2-B 3-O 4-AB）
     */
    private Integer bloodType;

    /**
     * Rh 血型（1-阳性 2-阴性）
     */
    private Integer rhType;

    /**
     * 血液成分（1-全血 2-红细胞悬液 3-洗涤红细胞 4-冰冻血浆 5-血小板 6-冷沉淀）
     */
    private Integer componentType;

    /**
     * 血量（ml）
     */
    private Integer volume;

    /**
     * 单位（U）
     */
    private java.math.BigDecimal unitAmount;

    /**
     * 采集日期
     */
    private LocalDate collectDate;

    /**
     * 失效日期
     */
    private LocalDate expireDate;

    /**
     * 血液来源（1-血站 2-自体储血 3-互助献血）
     */
    private Integer sourceType;

    /**
     * 来源单位 / 献血人
     */
    private String sourceName;

    /**
     * 献血码
     */
    private String donorNo;

    /**
     * 血型复核（0-未复核 1-已复核）
     */
    private Integer aboVerify;

    /**
     * 存放位置
     */
    private String storageLoc;

    /**
     * 状态（1-在库 2-已预留 3-已发血 4-已报废 5-已退回）
     */
    private Integer status;

    /**
     * 入库人
     */
    private String inboundBy;

    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;

    /**
     * 出库人
     */
    private String outboundBy;

    /**
     * 出库时间
     */
    private LocalDateTime outboundTime;

    /**
     * 关联用血申请单号
     */
    private String applyNo;
}
