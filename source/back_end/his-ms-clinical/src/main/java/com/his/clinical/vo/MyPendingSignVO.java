package com.his.clinical.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生「我的待补签」一行：本人书写、当前仍缺电子签名的病历/处方/检查检验申请单。
 */
@Data
public class MyPendingSignVO {

    /**
     * 签名对象类型（SignBizTypeEnum）
     */
    private Integer bizType;

    /** 对象类型文案 */
    private String bizTypeText;

    /**
     * 签名对象ID（雪花ID，前端按字符串处理）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    /** 单号（病历号/处方号/申请单号） */
    private String bizNo;

    /** 患者姓名 */
    private String patientName;

    /** 科室名称 */
    private String deptName;

    /** 业务状态码 */
    private Integer bizStatus;

    /** 业务状态文案 */
    private String bizStatusText;

    /** 就诊日期 */
    private LocalDate visitDate;

    /** 结诊/开单时间（列表按此倒序） */
    private LocalDateTime bizTime;
}
