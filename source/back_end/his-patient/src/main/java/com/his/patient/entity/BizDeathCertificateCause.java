package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 死亡证明死因链明细（死亡证明死因链，sql/157）。
 */
@Data
@TableName("biz_death_certificate_cause")
public class BizDeathCertificateCause implements Serializable {
    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 主键（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 死亡证明ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certId;

    /**
     * 部分（1-Ⅰ部分死因链 2-Ⅱ部分其他疾病）
     */
    private Integer part;

    /**
     * 行序：Ⅰ部分 1=a(直接死因) … 4=d(根本死因)，Ⅱ部分从 1 递增
     */
    private Integer seqNo;

    /**
     * ICD-10编码
     */
    private String icdCode;

    /**
     * 疾病或情况名称
     */
    private String icdName;

    /**
     * 发病至死亡间隔（文本）
     */
    private String intervalText;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
