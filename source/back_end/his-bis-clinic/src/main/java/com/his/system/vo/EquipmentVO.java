package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 设备档案 VO（台账行 + 详情）。
 */
@Data
public class EquipmentVO {

    /**
     * 主键
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
    private String categoryText;

    /**
     * 使用科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 使用科室名称
     */
    private String deptName;
    /**
     * 品牌
     */
    private String brand;
    /**
     * 型号
     */
    private String model;

    /**
     * 购置日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    /**
     * 购置价格(元)
     */
    private BigDecimal purchasePrice;
    /**
     * 状态（1-在用 2-停用 3-维修中 4-报废）
     */
    private Integer status;
    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 维保周期(天)
     */
    private Integer maintainCycleDays;

    /**
     * 最近维保日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastMaintainDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextMaintainDate;

    /**
     * 最近一次计量的有效期至（无计量记录为 null）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate meteringValidUntil;

    /**
     * 计量是否已过期
     */
    private Boolean meteringExpired;

    /**
     * 备注
     */
    private String remark;

    /**
     * 详情时附带：最近维保记录（最多 10 条）
     */
    private List<MaintainVO> recentMaintains;

    /**
     * 详情时附带：最近计量记录（最多 10 条）
     */
    private List<MeteringVO> recentMeterings;
}
