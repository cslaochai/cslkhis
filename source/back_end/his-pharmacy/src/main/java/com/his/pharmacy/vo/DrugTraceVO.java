package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 药品追溯码台账行VO（字段与台账实体一一对应，枚举文案由前端字典渲染）
 */
@Data
public class DrugTraceVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 院内追溯流水号 */
    private String traceNo;
    /** 追溯码原文 */
    private String traceCode;
    /** 码制（1-GS1 2-中国药品追溯码20位 3-其他） */
    private Integer codeType;
    /** 解析-产品标识 */
    private String drugDi;
    /** 解析-生产序列号 */
    private String serialNo;
    /** 解析-码内批号 */
    private String codeBatchNo;
    /** 解析-码内有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate codeExpiryDate;

    /** 药品ID */
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
    /** 批准文号 */
    private String approvalNumber;

    /** 采集挂靠批次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;
    /** 库存批号（快照） */
    private String stockBatchNo;
    /** 供应商（快照） */
    private String supplier;
    /** 供应商ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    /** 采集来源（1-入库采集 2-存量补采） */
    private Integer sourceType;
    /** 来源入库单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inboundId;
    /** 来源入库单号（快照） */
    private String inboundNo;

    /** 码状态（1-在库 2-已发药核销 3-已作废） */
    private Integer status;
    /** 采集扫码时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scanTime;
    /** 采集人 */
    private String operatorName;

    /** 发药单ID */
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dispenseTime;
    /** 发药核销人 */
    private String dispenseOperator;

    /** 上传状态（0-待上传 1-已上传 2-上传失败） */
    private Integer uploadStatus;
    /** 上传批次号 */
    private String uploadBatchNo;
    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;
    /** 上传失败原因 */
    private String uploadFailReason;

    /** 作废类型（1-退药 2-报损 3-召回） */
    private Integer voidType;
    /** 作废时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime voidTime;
    /** 作废原因 */
    private String voidReason;

    /** 备注 */
    private String remark;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
