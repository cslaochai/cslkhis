package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 麻精药品专册（台账）实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_narcotic_register")
public class BizNarcoticRegister extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 专册登记号（NZ + yyyyMMdd + 4 位序号）
     */
    private String registerNo;

    /**
     * 处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 发药记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispensingId;

    /**
     * 发药单号
     */
    private String dispensingNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（性别字典口径：1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 身份证号（麻醉药品与第一类精神药品按条例需实名登记）
     */
    private String idCard;

    /**
     * 开方科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 开方科室名称
     */
    private String deptName;

    /**
     * 临床诊断（麻精处方必须含诊断，缺则不得发药）
     */
    private String diagnosis;

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品编码
     */
    private String drugCode;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 单位
     */
    private String unit;

    /**
     * 特殊管理分类（1-麻醉 2-第一类精神 3-第二类精神 4-毒性）
     */
    private Integer specialFlag;

    /**
     * 剂型（用于判定限量档位：注射剂/控缓释/其他）
     */
    private String dosageForm;

    /**
     * 发药数量
     */
    private BigDecimal quantity;

    /**
     * 批号：FEFO 实际扣减批次，跨批次以逗号拼接。
     * <p>由后端从药品库存流水回查，**不取前端传值** ——
     * 麻精药品批号追溯是法定要求，让前端传等于让记录变成自述。
     */
    private String batchNo;

    /**
     * 处方疗程天数
     */
    private Integer duration;

    /**
     * 规则允许的最大天数（留痕：便于事后核对当时的判定依据）
     */
    private Integer limitDays;

    /**
     * 核定日用量（quantity / duration）
     */
    private BigDecimal dailyDosage;

    /**
     * 开方医师ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 开方医师姓名
     */
    private String doctorName;

    /**
     * 审方药师
     */
    private String auditBy;

    /**
     * 发药人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispenseById;

    /**
     * 发药人姓名
     */
    private String dispenseBy;

    /**
     * 发药时间
     */
    private LocalDateTime dispenseTime;

    /**
     * 复核人ID（双人复核，必须与发药人不同）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long checkerId;

    /**
     * 复核人姓名
     */
    private String checkerName;

    /**
     * 复核时间
     */
    private LocalDateTime checkTime;

    /**
     * 空安瓿回收状态（0-不适用 1-待回收 2-已回收）
     */
    private Integer ampouleStatus;

    /**
     * 发出安瓿数（麻醉药品注射剂 = 发药支数）
     */
    private BigDecimal ampouleIssued;

    /**
     * 回收空安瓿数
     */
    private BigDecimal ampouleReturned;

    /**
     * 剩余液销毁量
     */
    private BigDecimal ampouleDestroyed;

    /**
     * 回收登记人
     */
    private String returnBy;

    /**
     * 回收登记时间
     */
    private LocalDateTime returnTime;

    /**
     * 回收/销毁说明（双人销毁需写明见证人）
     */
    private String returnRemark;
}
