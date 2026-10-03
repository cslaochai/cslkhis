package com.his.equipment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医废登记 VO。
 */
@Data
public class WasteVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 医废交接单号 */
    private String wasteNo;

    /** 医废类别（1-感染性 2-损伤性 3-病理性 4-药物性 5-化学性） */
    private Integer wasteType;
    private String wasteTypeText;

    /** 重量（kg） */
    private BigDecimal weightKg;

    /** 产生科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /** 产生科室名称 */
    private String deptName;

    /** 收集时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectTime;

    /** 收集人 */
    private String collectorName;

    private Integer status;
    /** 状态文本 */
    private String statusText;

    /** 交接人 */
    private String handoverName;

    /** 交接时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handoverTime;

    /** 处置公司 */
    private String disposalCompany;

    /** 处置时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime disposalTime;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
