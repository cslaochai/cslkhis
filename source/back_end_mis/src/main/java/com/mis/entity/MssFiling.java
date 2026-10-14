package com.mis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 异地就医备案
 */
@Data
@TableName("mss_filing")
public class MssFiling implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;
    private String idCardMask;
    private String insuranceArea;
    private String hospital;
    private String reason;
    private String fromArea;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validStart;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validEnd;

    /** 状态（已备案/审核中/已失效） */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
