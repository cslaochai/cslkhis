package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 处方审方动作流水（L7 审方退回重开闭环，只增不改）。
 */
@Data
@TableName("biz_prescription_audit_log")
public class BizPrescriptionAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 处方 id（重提后是新 id）
     */
    private Long prescriptionId;

    /**
     * 处方号（重提后是新号）
     */
    private String prescriptionNo;

    /**
     * 病历 id（轮次关联锚，重提后不变）
     */
    private Long recordId;

    /**
     * 挂号 id
     */
    private Long registId;

    /**
     * 第几轮（1 起；重提 +1）
     */
    private Integer roundNo;

    /**
     * 动作(1审方通过/2审方退回/3退回后重提)
     */
    private Integer action;

    /**
     * 操作人员工 id（重提=开方医生）
     */
    private Long auditorId;

    /**
     * 操作人姓名
     */
    private String auditorName;

    /**
     * 审方意见/退回原因
     */
    private String opinion;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 删除标志
     */
    private Integer delFlag;
}
