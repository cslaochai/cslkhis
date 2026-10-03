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
 *
 * <p>字段与住院文书修改日志 **一一对应**。
 *
 * <p>为什么单独建表而不是复用门诊病历修改日志：那张表的记录ID
 * 语义是"门诊病历ID"、且没有单据类型列，护理文书混写进去后"这条日志属于谁"要靠猜。
 * 这里用 {@code docType} + {@code recordType} 两个码值把"哪类单据、哪种文书"说清楚。
 *
 * <p>写入策略：**只在字段值真的变了时写一行**（{@code fieldName/oldValue/newValue}）。
 * 不做"每次保存写全字段快照"——否则日志表会膨胀到无法回答"这句话是谁改的"。
 * 创建/提交/归档这类动作只写 {@code operation}、不写字段三件套。
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
