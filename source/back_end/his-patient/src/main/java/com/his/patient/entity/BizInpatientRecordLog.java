package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 住院文书修改留痕（病历文书 + 护理文书共用）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_record_log")
public class BizInpatientRecordLog extends BaseEntity implements Serializable {

    /**
     * 单据类型：1-住院病历文书 2-护理文书
     */
    private Integer docType;

    /**
     * 单据ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 单据号（便于按号追）
     */
    private String recordNo;

    /**
     * 文书类型码（住院病历 = record_type，护理 = nursing_type）
     */
    private Integer recordType;

    /**
     * 操作人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 操作人姓名
     */
    private String userName;

    /**
     * 操作：创建 / 修改 / 提交 / 归档
     */
    private String operation;

    /**
     * 变更字段（动作类留痕时为 NULL）
     */
    private String fieldName;

    /**
     * 变更前值
     */
    private String oldValue;

    /**
     * 变更后值
     */
    private String newValue;
}
