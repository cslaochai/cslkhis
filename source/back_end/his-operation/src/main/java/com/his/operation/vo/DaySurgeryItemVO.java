package com.his.operation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 日间手术准入目录 VO。
 */
@Data
public class DaySurgeryItemVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 术式编码
     */
    private String itemCode;

    /**
     * 术式名称
     */
    private String itemName;

    /**
     * 适用科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 适用科室名称（快照）
     */
    private String deptName;

    /**
     * 最长滞留小时数
     */
    private Integer maxStayHours;

    /**
     * 手术级别（1~4，字典 his_operation_level）
     */
    private Integer operationLevel;

    /**
     * 麻醉方式（1-局部麻醉 2-椎管内麻醉 3-全身麻醉 4-神经阻滞 5-其他）
     */
    private Integer anesthesiaType;

    /**
     * 标准费用
     */
    private BigDecimal standardFee;

    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
