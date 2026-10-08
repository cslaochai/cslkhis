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

/**
 * 静配中心主单（PIVAS）。
 *
 * <p>粒度「一次住院 × 一个调配日」一张，同入院同日复用、明细追加（与住院摆药同口径）。
 * 主单 status 是聚合派生值，随明细状态实时回算（见 PivasServiceImpl#applyAggregatedStatus）：
 * 1-待审方 2-待排队 3-待调配 4-待核对 5-已完成 6-全拒配。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pivas_batch")
public class BizPivasBatch extends BaseEntity implements Serializable {

    /** 主单状态（聚合派生）：1-待审方 */
    public static final int STATUS_PENDING_AUDIT = 1;
    /** 主单状态：2-待排队 */
    public static final int STATUS_PENDING_QUEUE = 2;
    /** 主单状态：3-待调配 */
    public static final int STATUS_PENDING_COMPOUND = 3;
    /** 主单状态：4-待核对 */
    public static final int STATUS_PENDING_VERIFY = 4;
    /** 主单状态：5-已完成 */
    public static final int STATUS_DONE = 5;
    /** 主单状态：6-全拒配 */
    public static final int STATUS_ALL_REJECTED = 6;

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
