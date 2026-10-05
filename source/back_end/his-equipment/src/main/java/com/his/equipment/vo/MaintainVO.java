package com.his.equipment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备维保记录 VO。
 */
@Data
public class MaintainVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 设备ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long equipmentId;

    /**
     * 设备编码（快照）
     */
    private String equipmentCode;
    /**
     * 设备名称（快照）
     */
    private String equipmentName;

    /**
     * 维保类型（1-保养 2-维修 3-巡检）
     */
    private Integer maintainType;
    private String maintainTypeText;

    /**
     * 维保日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate maintainDate;

    /**
     * 下次维保日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextMaintainDate;

    /**
     * 费用（元）
     */
    private BigDecimal cost;
    /**
     * 故障描述
     */
    private String faultDesc;
    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 维保结果（1-正常 2-异常）
     */
    private Integer maintainResult;
    private String maintainResultText;

    /**
     * 维保人
     */
    private String handlerName;
    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
