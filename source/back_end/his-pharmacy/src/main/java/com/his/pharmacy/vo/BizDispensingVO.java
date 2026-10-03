package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发药信息出参
 */
@Data
public class BizDispensingVO {
    /** 发药单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 发药单号 */
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
    /** 处方类型 */
    private Integer prescriptionType;
    /** 开方医生姓名 */
    private String doctorName;
    /** 发药总金额，单位：元 */
    private BigDecimal totalAmount;
    /** 药品种类数 */
    private Integer drugCount;
    /** 发药状态 */
    private Integer dispensingStatus;
    /** 审核人 */
    private String auditBy;
    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    /** 发药人 */
    private String dispenseBy;
    /** 发药时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dispenseTime;
    /** 取药人 */
    private String pickUpBy;
    /** 取药时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickUpTime;
    /** 发药明细列表 */
    private List<BizDispensingDetailVO> details;
}
