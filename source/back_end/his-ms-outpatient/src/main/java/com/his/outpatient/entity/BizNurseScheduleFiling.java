package com.his.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 护理排班周备案台账：一个护理单元（病区/门诊科室）的一个排班周一条，
 * 记录片区审核发布后护理部的备案确认、退回与经办人。
 *
 * <p>WHY：业务唯一键「单元类型 × 护理单元 × 周起始日」不含删除标志，本表只 upsert 不删；
 * 确需删行必须物理删，软删会让同键重插撞唯一键。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nurse_schedule_filing")
public class BizNurseScheduleFiling extends BaseEntity {

    /**
     * 排班单元类型（1-病区 2-门诊科室）
     */
    private Integer unitType;

    /**
     * 护理单元ID
     */
    private Long wardId;

    /**
     * 护理单元名称（快照）
     */
    private String wardName;

    /**
     * 所属科室ID
     */
    private Long deptId;

    /**
     * 所属科室名称（快照）
     */
    private String deptName;

    /**
     * 周起始日（周一）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    /**
     * 周结束日（周日）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEnd;

    /**
     * 备案态（0-待备案 1-已备案 2-已退回）
     */
    private Integer filingStatus;

    /**
     * 备案人（员工档案ID）
     */
    private Long filerId;

    /**
     * 备案人姓名（快照）
     */
    private String filerName;

    /**
     * 备案时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime filingTime;

    /**
     * 备案意见/退回原因
     */
    private String opinion;

    /**
     * 逻辑删除标志（0-正常 1-已删除；业务只 upsert 不删）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
