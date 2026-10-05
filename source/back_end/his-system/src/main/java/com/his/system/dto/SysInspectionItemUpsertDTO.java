package com.his.system.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 检查项目新增/修改入参
 */
@Data
public class SysInspectionItemUpsertDTO {

    /**
     * 检查项目ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 项目编码（唯一）
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 项目类型（1-放射检查 2-超声检查 3-心电图 4-内镜检查 5-其他）
     */
    private Integer itemType;

    /**
     * 检查科室ID
     */
    private Long deptId;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 检查价格，单位：元
     */
    private BigDecimal price;

    /**
     * 检查耗时（分钟）
     */
    private Integer duration;

    /**
     * 检查前准备事项
     */
    private String preparation;

    /**
     * 禁忌症
     */
    private String contraindication;

    /**
     * 是否支持急诊（0-否 1-是）
     */
    private Integer isEmergency;

    /**
     * 是否需要预约（0-否 1-是）
     */
    private Integer isAppointment;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
}
