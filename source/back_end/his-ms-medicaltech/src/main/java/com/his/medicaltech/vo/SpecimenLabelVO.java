package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标本标签渲染出参：字段即标签纸上能印的内容，不含版式信息。
 */
@Data
public class SpecimenLabelVO {

    /**
     * 检验记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 标本号（标签码值）
     */
    private String specimenNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别：1-男 2-女
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 开单科室
     */
    private String applyDeptName;

    /**
     * 检验项目
     */
    private String laboratoryItemName;

    /**
     * 标本类型
     */
    private String specimenType;

    /**
     * 采样时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sampleTime;

    /**
     * 已打印次数（0 = 还没打过；>0 时前端必须先问补打原因）
     */
    private Integer printCount;

    /**
     * 本次打印序号（仅打印接口返回）
     */
    private Integer printNo;

    /**
     * 纸张规格
     */
    private String paperSpec;

    /**
     * 渲染器标识
     */
    private String renderCode;
}
