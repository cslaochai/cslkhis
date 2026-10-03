package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 住院文书修改日志 VO。
 *
 * <p>飞检问的是"这句话是谁什么时候改的、原来写的是什么" —— 所以 {@code oldValue/newValue}
 * 原样给出，不做截断与美化。
 */
@Data
public class InpatientRecordLogVO implements Serializable {

    /**
     * 日志ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 单据类型：1-住院病历文书 2-护理文书
     */
    private Integer docType;

    /**
     * 单据类型文案
     */
    private String docTypeText;

    /**
     * 单据ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 单据号
     */
    private String recordNo;

    /**
     * 文书类型码
     */
    private Integer recordType;

    /**
     * 变更字段编码（= 库列名，动作类留痕时为 null）
     */
    private String fieldName;

    /**
     * 变更字段中文名（把列名翻成人看得懂的名字；未知列名原样返回，不猜）
     */
    private String fieldLabel;

    /**
     * 操作
     */
    private String operation;

    /** 操作人ID（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 操作人姓名
     */
    private String userName;

    /**
     * 变更前值
     */
    private String oldValue;

    /**
     * 变更后值
     */
    private String newValue;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;
}
