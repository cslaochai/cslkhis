package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 剂量上限知识条目（列表/详情出参）
 */
@Data
public class DoseLimitVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 成分关键字
     */
    private String component;

    /**
     * 剂量单位（g/mg/ug，字典 his_dose_unit）
     */
    private String doseUnit;

    /**
     * 单次最大量
     */
    private BigDecimal maxSingleDose;

    /**
     * 每日最大量
     */
    private BigDecimal maxDailyDose;

    /**
     * 口径说明（同一成分在不同适应证下极量差一个量级，界面必须显示）
     */
    private String note;

    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 该成分在本院药品字典里能命中多少条药品；0=这条上限暂时打不到任何药
     */
    private Integer drugHits;
}
