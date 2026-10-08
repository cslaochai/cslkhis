package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 超声结构化测量值（超声测量值）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ultrasound_measure")
public class BizUltrasoundMeasure extends BaseEntity {

    /**
     * 超声记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 超声检查号
     */
    private String recordNo;

    /**
     * 测量项（如肝右叶斜径 / EF / 结节大小）
     */
    private String measureName;

    /**
     * 测量值
     */
    private String measureValue;

    /**
     * 单位
     */
    private String unit;

    /**
     * 参考范围
     */
    private String referenceRange;

    /**
     * 异常标志（0-正常 1-偏高 2-偏低 3-异常）
     */
    private Integer abnormalFlag;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
