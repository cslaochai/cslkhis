package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检查项目出参
 */
@Data
public class SysInspectionItemVO {

    /**
     * 检查项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

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
    @JsonSerialize(using = ToStringSerializer.class)
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
