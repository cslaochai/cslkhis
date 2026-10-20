package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 静配中心主单（PIVAS）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pivas_batch")
public class BizPivasBatch extends BaseEntity implements Serializable {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /**
     * 静配单号（PV + yyyyMMdd + 4位流水号）
     */
    private String pivasNo;

    /**
     * 调配日期（长期医嘱按日排的锚点）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate admixDate;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /** 病区ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称
     */
    private String wardName;

    /** 主单状态（1-待审方 2-待排队 3-待调配 4-待核对 5-已完成 6-全拒配） */
    private Integer status;

    /**
     * 明细条数（冗余，列表展示）
     */
    private Integer itemCount;

    /**
     * 生成人（静配中心接收）
     */
    private String generateBy;

    /** 生成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generateTime;

    /**
     * 打标签（排队）操作人
     */
    private String labelBy;

    /**
     * 打标签时间（标签打印预留：取号盖时间，不接打印机）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime labelTime;
}
