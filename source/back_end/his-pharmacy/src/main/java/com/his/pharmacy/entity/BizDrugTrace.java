package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 药品追溯码台账（一码一行：入库扫码采集 → 发药扫码核销 → 医保上传）
 *
 * <p>医保局口径：入库采集、发药核销两个动作都要扫码上传。
 * 本表是这两件事的唯一留痕，与耗材 UDI 台账是两套码制两套台账，
 * 不要互相冒充：药品追溯码是 20 位/GS1 的产品级追溯，UDI 是医疗器械一物一码。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_drug_trace")
public class BizDrugTrace extends BaseEntity {

    /** 院内追溯流水号（DR+时间戳） */
    private String traceNo;
    /** 追溯码原文（扫码枪整串留底） */
    private String traceCode;
    /** 码制（1-GS1 2-中国药品追溯码20位 3-其他/未识别） */
    private Integer codeType;
    /** 解析-产品标识（GS1 (01) GTIN-14 / 20位码前8位本体码） */
    private String drugDi;
    /** 解析-生产序列号（GS1 (21) / 20位码后12位） */
    private String serialNo;
    /** 解析-码内批号（GS1 (10)） */
    private String codeBatchNo;
    /** 解析-码内有效期（GS1 (17)） */
    private LocalDate codeExpiryDate;

    /** 药品ID（药品字典主键） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;
    /** 药品编码（快照） */
    private String drugCode;
    /** 药品名称（快照） */
    private String drugName;
    /** 通用名（快照） */
    private String genericName;
    /** 规格（快照） */
    private String specification;
    /** 剂型（快照） */
    private String dosageForm;
    /** 单位（快照） */
    private String unit;
    /** 生产厂家（快照） */
    private String manufacturer;
    /** 批准文号（快照，医保上传必备） */
    private String approvalNumber;

    /** 采集挂靠批次ID（药品批次库存主键） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;
    /** 库存批号（快照） */
    private String stockBatchNo;
    /** 供应商（快照） */
    private String supplier;
    /** 供应商ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    /** 采集来源（1-入库采集 2-存量补采） */
    private Integer sourceType;
    /** 来源入库单ID（药品入库单主键） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inboundId;
    /** 来源入库单号（快照） */
    private String inboundNo;

    /** 码状态（1-在库 2-已发药核销 3-已作废：退药/报损/召回，不可再发） */
    private Integer status;
    /** 采集扫码时间 */
    private LocalDateTime scanTime;
    /** 采集人 */
    private String operatorName;

    /** 发药单ID（药品发药记录主键，跨模块快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispensingId;
    /** 发药单号（快照） */
    private String dispensingNo;
    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /** 患者编号（快照） */
    private String patientNo;
    /** 患者姓名（快照） */
    private String patientName;
    /** 就诊类型（1-门诊 2-住院） */
    private Integer visitType;
    /** 门诊挂号ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;
    /** 住院ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
    /** 发药科室ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /** 发药科室名称（快照） */
    private String deptName;
    /** 发药核销时间 */
    private LocalDateTime dispenseTime;
    /** 发药核销人 */
    private String dispenseOperator;

    /** 上传状态（0-待上传 1-已上传 2-上传失败） */
    private Integer uploadStatus;
    /** 上传批次号（同一次批量上传） */
    private String uploadBatchNo;
    /** 上传时间 */
    private LocalDateTime uploadTime;
    /** 上传失败原因（截断到列宽） */
    private String uploadFailReason;

    /** 作废类型（1-退药 2-报损 3-召回） */
    private Integer voidType;
    /** 作废时间 */
    private LocalDateTime voidTime;
    /** 作废原因 */
    private String voidReason;
}
