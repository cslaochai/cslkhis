package com.his.supplies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 高值耗材使用登记入参（一件耗材一条台账，扣批次库存 1 件并触发计费）
 */
@Data
public class HighValueUseDTO {
    /** UDI 原文（留底+防重复扫码） */
    @NotBlank(message = "UDI码不能为空，请先扫码")
    private String udiCode;
    /** 耗材ID（须为高值耗材） */
    @NotNull(message = "请选择耗材字典中的高值耗材")
    private Long consumableId;
    /** 出库批次ID（该耗材名下有货的批次） */
    @NotNull(message = "请选择出库批次")
    private Long stockId;
    /** 患者ID */
    @NotNull(message = "请选择患者")
    private Long patientId;
    /** 患者编号（快照，前端从患者选择带出；缺失后端回查兜底） */
    private String patientNo;
    /** 患者姓名（快照） */
    private String patientName;
    /** 就诊类型（1-门诊 2-住院），空=不挂就诊、不计费 */
    private Integer visitType;
    /** 门诊挂号ID（visitType=1 必填才有计费锚点） */
    private Long registId;
    /** 门诊挂号单号（快照） */
    private String registNo;
    /** 住院ID（visitType=2 必填才有计费锚点） */
    private Long admissionId;
    /** 使用科室ID */
    private Long deptId;
    /** 用途/手术备注 */
    private String purpose;
}
