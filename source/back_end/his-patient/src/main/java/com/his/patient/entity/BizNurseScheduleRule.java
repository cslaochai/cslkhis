package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 病区护理人力配置标准（护理人力配置标准，sql/166）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nurse_schedule_rule")
public class BizNurseScheduleRule extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * shift_id=0 表示病区级规则行
     */
    public static final long WARD_LEVEL_SHIFT_ID = 0L;

    /**
     * 病区ID
     */
    private Long wardId;
    /**
     * 病区名称
     */
    private String wardName;
    /**
     * 班次ID
     */
    private Long shiftId;
    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 最低在岗人数
     */
    private Integer minStaff;
    /**
     * 最高在岗人数
     */
    private Integer maxStaff;
    /**
     * 单周工时上限（小时，仅病区级行有效）
     */
    private BigDecimal maxWeekHours;
    /**
     * 连续夜班天数上限
     */
    private Integer maxConsecutiveNightDays;
    /**
     * 连续上班天数上限
     */
    private Integer maxConsecutiveWorkDays;
    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
