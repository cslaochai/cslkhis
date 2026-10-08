package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 病区护理人力配置标准（护理人力配置标准，sql/166）。
 *
 * <p>一个病区两层规则行，靠班次ID 区分：
 * <ul>
 *   <li>{@code shift_id=0} <b>病区级</b>行：全病区每日在岗总人数下限/上限 + 单周工时上限 +
 *       连续夜班/连续上班天数上限（这些是"人"的约束，不属于某个班次）；</li>
 *   <li>{@code shift_id>0} <b>班次级</b>行：每个班次每日至少要几个人（后夜班只排 1 人是有依据的，
 *       不是拍脑袋——「最低 1 人」写进规则，缺人时才会报警）。</li>
 * </ul>
 *
 * <p>唯一键 {@code uk_ward_shift_rule(ward_id, shift_id)} <b>不含 del_flag</b>，
 * 所以本表删除走物理删（见 Mapper 的 {@code purgeById}），软删会让"删了再建同一班次规则"撞键。
 *
 * <p>规则是<b>告警依据不是拦截器</b>：急诊抽调、临时加床都会造成合理的缺口，
 * 排班页要能继续保存，同时把缺口显式列出来给护理部看。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nurse_schedule_rule")
public class BizNurseScheduleRule extends BaseEntity {

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
