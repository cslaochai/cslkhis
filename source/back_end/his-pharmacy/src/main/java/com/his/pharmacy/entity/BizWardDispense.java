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
 * 住院摆药单主单（G13）。
 *
 * <p>粒度是「一次住院 × 一天」一张：药房按病区/日期生成，同入院的同日主单**复用**、明细追加。
 * 主单状态是**聚合派生值**，随明细状态实时回算（见 WardDispenseServiceImpl#applyAggregatedStatus），
 * 不单独维护状态机 —— 明细状态才是事实源。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ward_dispense")
public class BizWardDispense extends BaseEntity implements Serializable {

    /**
     * 摆药单号（WD + yyyyMMdd + 4位流水号）
     */
    private String dispenseNo;

    /**
     * 摆药日期（长期医嘱按日一摆的锚点）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseDate;

    /**
     * 入院ID（一次住院 × 一天一张主单）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号（快照）
     */
    private String patientNo;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 病区ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称（快照）
     */
    private String wardName;

    /**
     * 入院科室ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 主单状态（聚合派生）:1-待配药 2-配药中 3-已配药 4-已核对 5-已退药
     */
    private Integer status;

    /**
     * 生成人（药房）
     */
    private String generateBy;

    /**
     * 生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generateTime;
}
