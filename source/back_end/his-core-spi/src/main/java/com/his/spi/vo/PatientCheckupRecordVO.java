package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 患者端-体检报告（登记单 + 总检结论 + 逐项结果）。
 */
@Data
public class PatientCheckupRecordVO {

    /**
     * 体检登记ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 患者ID（供调用方做归属校验，前端无需渲染）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 体检单号
     */
    private String recordNo;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 体检日期（yyyy-MM-dd）
     */
    private String checkupDate;

    /**
     * 合计金额（元）
     */
    private BigDecimal totalAmount;

    /**
     * 登记状态码值（1-已登记 2-检查中 3-已完成 4-已出报告）
     */
    private Integer recordStatus;

    /**
     * 登记状态文案
     */
    private String recordStatusText;

    /**
     * 总检结论
     */
    private String conclusion;

    /**
     * 总检医生
     */
    private String doctorName;

    /**
     * 报告时间（yyyy-MM-dd HH:mm:ss）
     */
    private String reportTime;

    /**
     * 逐项结果（列表页为空，详情页填充）
     */
    private List<PatientCheckupItemVO> items;
}
