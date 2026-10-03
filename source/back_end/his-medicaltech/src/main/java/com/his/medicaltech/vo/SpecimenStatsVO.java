package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标本统计出参
 */
@Data
public class SpecimenStatsVO {
    /**
     * 主键ID
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
     * 今日标本总数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long todayCount;

    /**
     * 待采集数量
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pendingSample;

    /**
     * 已采集数量
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sampled;

    /**
     * 检验中数量
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long testing;

    /**
     * 异常标本数量
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long abnormal;
}
