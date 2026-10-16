package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 临床路径模板定义（临床路径模板）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pathway")
public class BizPathway extends BaseEntity implements Serializable {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 路径编码
     */
    private String pathwayCode;

    /**
     * 路径名称
     */
    private String pathwayName;

    /**
     * 适用科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 适用科室名称
     */
    private String deptName;

    /**
     * 适用病种/诊断口径（中文描述，入径时医生判断）
     */
    private String diagnosis;

    /**
     * 版本号
     */
    private String version;

    /**
     * 路径总日数（发布时按步骤 max(day_no) 回算固化）
     */
    private Integer totalDays;

    /**
     * 状态（1-草稿 2-使用中 3-已停用）
     */
    private Integer status;

    /**
     * 发布人
     */
    private String publishBy;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
