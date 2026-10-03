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

/**
 * 临床路径模板定义（临床路径模板）。
 *
 * <p>编码+版本一张单：同 pathway_code 只允许一张「使用中」（服务层校验），
 * 升版本 = 新草稿再发布。状态机 1草稿（可编辑）→ 2使用中（锁定、可入径）→ 3已停用。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pathway")
public class BizPathway extends BaseEntity implements Serializable {

    /** 路径编码 */
    private String pathwayCode;

    /** 路径名称 */
    private String pathwayName;

    /** 适用科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 适用科室名称（快照） */
    private String deptName;

    /** 适用病种/诊断口径（中文描述，入径时医生判断） */
    private String diagnosis;

    /** 版本号 */
    private String version;

    /** 路径总日数（发布时按步骤 max(day_no) 回算固化） */
    private Integer totalDays;

    /** 状态（1-草稿 2-使用中 3-已停用） */
    private Integer status;

    /** 发布人 */
    private String publishBy;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
