package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 各检查设备已开展项目数（BizExamDeviceMapper#countItemsByDevice 一行）。
 */
@Data
public class ExamDeviceItemCountRowVO implements Serializable {

    /**
     * 设备ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deviceId;

    /**
     * 已开展项目数
     */
    private Long itemCount;
}