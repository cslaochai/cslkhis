package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检查胶片用量与发放（检查胶片用量，sql/138）。
 *
 * <p>它挂记录ID（执行记录）而不是申请单：胶片是「这一次拍片之后发的」，
 * 而同一个申请单可能挂着重做/补拍的多条记录（库里实测一个 apply_id 最多 5 条），
 * 挂申请单会分不清是哪次拍的片、该找谁要钱。
 *
 * <p>患者 / 项目 / 规格名 / 单价一律快照：价目表改名、患者改名都不能改写已经发出去的胶片行
 * —— 胶片是实物，发了就是发了，账单上的名字必须和当时交给患者的那张一致。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exam_film")
public class BizExamFilm extends BaseEntity {

    /** 胶片单号（FM+yyyyMMdd+5位，唯一） */
    private String filmNo;

    /** 检查记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /** 检查记录号（快照） */
    private String recordNo;

    /** 检查申请单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /** 申请单号（快照） */
    private String applyNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 就诊日期（快照） */
    private LocalDate visitDate;

    /** 检查项目编码（快照） */
    private String itemCode;

    /** 检查项目名称（快照） */
    private String itemName;

    /** 检查部位（快照） */
    private String bodyPart;

    /** 影像模态（字典 his_exam_device_type，快照） */
    private Integer modality;

    /** 胶片规格ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long specId;

    /** 规格编码（快照） */
    private String specCode;

    /** 规格名称（快照，列表直接显示，不反查价目表） */
    private String specName;

    /** 单价（记账当时的规格价，快照） */
    private BigDecimal unitPrice;

    /** 计价单位（快照） */
    private String unit;

    /** 胶片张数 */
    private Integer quantity;

    /** 金额 = 单价 × 张数（服务端现算；改错走红冲，不 UPDATE 这一列） */
    private BigDecimal amount;

    /** 状态（字典 his_film_status：1-已登记 2-已打印 3-已发放 4-已作废） */
    private Integer filmStatus;

    /** 是否已记账（0-未记账 1-已记账） */
    private Integer chargeFlag;

    /** 记账流水ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeId;

    /** 记账流水号（快照） */
    private String feeNo;

    /** 打印人 */
    private String printBy;

    /** 打印时间 */
    private LocalDateTime printTime;

    /** 发放人（交给患者/病区的那个人） */
    private String deliverBy;

    /** 发放时间 */
    private LocalDateTime deliverTime;
}
