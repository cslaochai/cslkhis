package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发药信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dispensing")
public class BizDispensing extends BaseEntity {

    private String dispensingNo;

    /** 处方ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /** 处方号 */
    private String prescriptionNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /** 患者编号 */
    private String patientNo;
    /** 患者姓名 */
    private String patientName;
    private Integer prescriptionType;
    /** 医生姓名 */
    private String doctorName;
    /** 合计金额 */
    private BigDecimal totalAmount;
    private Integer drugCount;
    private Integer dispensingStatus;
    private String auditBy;
    /** 审核时间 */
    private LocalDateTime auditTime;
    private String dispenseBy;
    private LocalDateTime dispenseTime;
    private String pickUpBy;
    private LocalDateTime pickUpTime;

    @TableField(exist = false)
    private List<BizDispensingDetail> details;
}
