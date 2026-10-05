package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 床位调配台账
 *
 * <p>每一张被"预留/借走"的床，都要有一行能回答四个问题：<b>谁的床、谁在用、谁安排的、什么时候还的</b>。
 * 没有这张台账时，跨科借床在系统里是隐形的 —— 骨科的床上躺着呼吸内科的患者，
 * 而两侧科室的床位统计都认为这张床是自己的人在用，月底谁也说不清占用率怎么算出来的。
 *
 * <p><b>own_dept_id 与 use_dept_id 必须分开</b>：前者是床位的归属科室（资产口径，不可变），
 * 后者是这次实际使用的科室（借入口径）。合并成一个 dept_id 的话，跨科调配就会把
 * 「床位属于谁」这件事实覆盖掉 —— 与病案首页 admit_dept_id / dept_id 分开是一把尺子。
 *
 * <p><b>释放也是留痕</b>：取消/退回队列时把行推进到 3-已释放并写 release_reason/release_time，
 * 不是把行删掉。调配台账是给人查"这张床上周被谁拿走过"用的，删了就没得查。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_bed_allocate")
public class BizBedAllocate extends BaseEntity {

    /**
     * 调配单号（TP + yyyyMMdd + 3位序号）
     */
    private String allocateNo;

    /**
     * 床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /**
     * 床位号（快照）
     */
    private String bedNo;

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
     * 床位归属科室ID（资产口径）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ownDeptId;

    /**
     * 床位归属科室名称（快照）
     */
    private String ownDeptName;

    /**
     * 实际使用科室ID（跨科调配时 ≠ ownDeptId）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long useDeptId;

    /**
     * 实际使用科室名称（快照）
     */
    private String useDeptName;

    /**
     * 来源等床记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long waitId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 调配类型（1-本科室预留 2-跨科调配 3-急诊占床）
     */
    private Integer allocType;

    /**
     * 状态（1-已预留 2-已转入院 3-已释放 4-已作废）
     */
    private Integer allocStatus;

    /**
     * 操作人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 操作人姓名（快照）
     */
    private String operatorName;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operateTime;

    /**
     * 释放时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime releaseTime;

    /**
     * 释放/作废原因
     */
    private String releaseReason;

    /**
     * 转入院后的入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
}
