package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 诊室
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_clinic_room")
public class SysClinicRoom extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 诊室名称，如：心内科一诊室、302诊室
     */
    private String name;

    /**
     * 呼叫代号（队列号前缀），如 A/B/ER。用于门诊叫号大屏显示 "A001"。
     */
    private String queuePrefix;

    /**
     * 诊室编号（ABCD）
     */
    private String code;

    /**
     * 地理位置，如：门诊楼A座3层
     */
    private String location;

    /**
     * 所属科室ID，关联科室表
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 诊室状态：1-启用，0-停用（如装修/维修中）
     */
    private Integer status;
}
