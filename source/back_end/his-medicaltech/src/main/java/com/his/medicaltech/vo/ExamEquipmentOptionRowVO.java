package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 医疗设备台账候选项（{@code BizExamDeviceMapper#selectEquipmentOptions} 一行）。
 *
 * <p>只读挂接医疗设备台账，档案归 G22 域，本模块不建不改。列名先对过 information_schema：
 * 台账只有 {@code equipmentCode / equipmentName / category / status / deptId} 等，
 * <b>没有 unit、spec</b> —— 裸 SQL 猜列名会编译期不报错、运行期 Unknown column 被兜成 500。
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