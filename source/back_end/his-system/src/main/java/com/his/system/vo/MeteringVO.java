package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备计量记录 VO。
 */
@Data
public class MeteringVO {

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
     * 计量类型（1-强检 2-校准）
     */
    private Integer meteringType;
    private String meteringTypeText;

    /**
     * 计量日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate meteringDate;

    /**
     * 有效期至
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    /**
     * 是否已过期（validUntil < 今天）
     */
    private Boolean expired;

    /**
     * 计量结果（1-合格 2-不合格）
     */
    private Integer meteringResult;
    private String meteringResultText;

    /**
     * 证书编号
     */
    private String certNo;
    /**
     * 检定/校准机构
     */
    private String agency;
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
