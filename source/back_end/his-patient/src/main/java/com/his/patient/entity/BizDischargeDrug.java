package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出院带药单实体（一条一行药品项；挂入院次，出院前即可开单）。
 */
@Data
@TableName("biz_discharge_drug")
public class BizDischargeDrug {

    /**
     * 带药单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 带药单号
     */
    private String orderNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

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
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;
    /**
     * 药品名称
     */
    private String drugName;
    /**
     * 规格
     */
    private String spec;
    /**
     * 每次剂量/用法用量描述
     */
    private String dosage;
    /**
     * 单位
     */
    private String unit;
    /**
     * 带药数量
     */
    private BigDecimal quantity;
    /**
     * 用药医嘱
     */
    private String usageText;
    /**
     * 用药天数
     */
    private Integer days;
    /**
     * 备注
     */
    private String remark;

    /**
     * 发药状态（1-待发药 2-已发药）
     */
    private Integer dispenseStatus;

    /**
     * 发药人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispenseBy;
    /**
     * 发药人姓名
     */
    private String dispenseName;
    /**
     * 发药时间
     */
    private LocalDateTime dispenseTime;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
}
