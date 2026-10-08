package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 病案编码任务实体
 * <p>
 * 状态机：1 待编码 →（提交编码）→ 2 已提交 →（审核）→ 3 已完成 / 4 已退修；
 * 已退修可再次提交（→2），每次退修 return_count+1 留痕。
 */
@Data
@TableName("biz_archive_code_task")
public class BizArchiveCodeTask {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 任务号 CT+yyyyMMdd+4位
     */
    private String taskNo;

    /**
     * 归档记录病历归档的ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long archiveId;

    /**
     * 病历号
     */
    private String recordNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 病历所属科室
     */
    private String deptName;

    /**
     * 病历诊断（快照，编码前的人工参照）
     */
    private String diagnosis;

    /**
     * 状态（1-待编码 2-已提交 3-已完成 4-已退修）
     */
    private Integer status;

    /**
     * 编码人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long coderId;

    /**
     * 编码员姓名
     */
    private String coderName;

    /**
     * 分配时间
     */
    private LocalDateTime assignTime;

    /**
     * 主诊断 ICD-10 编码
     */
    private String mainIcdCode;

    /**
     * 主诊断名称
     */
    private String mainIcdName;

    /**
     * 其他诊断/手术 ICD（文本，分号分隔）
     */
    private String otherIcdText;

    /**
     * 提交编码时间
     */
    private LocalDateTime submitTime;

    /**
     * 审核人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditById;

    /**
     * 审核人姓名
     */
    private String auditByName;

    /**
     * 审核意见（退修必填）
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 累计退修次数
     */
    private Integer returnCount;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
