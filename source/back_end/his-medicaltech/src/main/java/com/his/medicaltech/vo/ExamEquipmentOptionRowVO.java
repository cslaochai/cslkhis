package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 医疗设备台账候选项（BizExamDeviceMapper#selectEquipmentOptions 一行）。
 */
@Data
public class ExamEquipmentOptionRowVO implements Serializable {

    /**
     * 台账主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 设备编码
     */
    private String equipmentCode;

    /**
     * 设备名称
     */
    private String equipmentName;

    /**
     * 设备类别
     */
    private Integer category;

    /**
     * 状态（1-在用 2-停用 3-维修中 4-报废）
     */
    private Integer status;
}