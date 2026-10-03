package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字段级修改日志出参（字段级修改日志，sql/159）。
 *
 * <p>一行 = 一个字段的一次变化。「改了 5 个字段」会落 5 行，靠 {@code batchNo} 归成一组 ——
 * 审计员要的是"这一刀改了什么"，不是"这个字段被改过"。
 */
@Data
public class FieldChangeVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 对象类型：PATIENT / USER / MEDICAL_RECORD / INPATIENT_RECORD */
    private String bizType;

    private String bizTypeText;

    /** 对象ID */
    private String bizId;

    /** 对象编号快照（患者号/工号/病历号） */
    private String bizNo;

    /** 对象名称快照（患者姓名/用户名） */
    private String bizName;

    /** 字段英文名 */
    private String fieldName;

    /** 字段中文名 */
    private String fieldLabel;

    /** 变更前值（直接标识符已打码；null 表示原来没值） */
    private String oldValue;

    /** 变更后值（null 表示改后清空） */
    private String newValue;

    /** 变更类型 INSERT / UPDATE / ACTION */
    private String changeType;

    private String changeTypeText;

    /** 批次号：同一次保存的多个字段共用 */
    private String batchNo;

    /** 操作人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 操作人科室名称 */
    private String deptName;

    /** 变更时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime changeTime;

    /** 备注 */
    private String remark;
}
