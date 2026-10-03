package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日间手术随访台账（出院/转住院后 24h 内必访，追加式可多次）。
 */
@Data
@TableName("biz_day_surgery_follow")
public class BizDaySurgeryFollow {

    /** 随访结果：无异常 */
    public static final int RESULT_NORMAL = 1;

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 登记单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /** 随访方式（1-电话 2-门诊 3-上门 4-线上） */
    private Integer followType;

    /** 随访结果（1-无异常 2-有异常已处置 3-有异常再就诊 4-失联） */
    private Integer result;

    /** 随访内容 */
    private String content;

    /** 随访人（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /** 随访人姓名 */
    private String operator;

    /** 随访时间 */
    private LocalDateTime followTime;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标志（0-正常 1-删除） */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /** 备注 */
    private String remark;
}
