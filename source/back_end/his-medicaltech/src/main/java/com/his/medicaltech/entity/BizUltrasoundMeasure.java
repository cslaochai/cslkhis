package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 超声结构化测量值（超声测量值）
 *
 * <p>为什么单独一张表而不是塞进 findings 文本：测量值要能统计、能比对复查趋势，
 * 埋在自由文本里就只能靠人眼读。异常标志 abnormal_flag 由服务端按参考范围判定，
 * 前端不做判定。
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
