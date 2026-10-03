package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 诊室信息出参
 */
@Data
public class ClinicRoomVO {

    /**
     * 诊室ID
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

    /** 备注信息 */
    private String remark;

    /**
     * 诊室名称，如：心内科一诊室、302诊室
     */
    private String name;

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

    /** 诊室状态（1-启用 0-停用） */
    private Integer status;
}
