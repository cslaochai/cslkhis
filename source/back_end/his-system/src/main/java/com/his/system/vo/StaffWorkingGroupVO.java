package com.his.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 在岗人次聚合行（{@code BizStaffScheduleMapper#groupWorkingByUnitShift} 的返回行）。
 *
 * <p>对应 SQL：{@code biz_staff_schedule} 按 {@code schedule_date × org_type × org_id ×
 * shift_id × staff_type} 的 group by 统计，一次扫描同时喂给三个消费方 ——
 * 单元×日在岗矩阵（{@code StaffScheduleServiceImpl}）、岗位×日在岗矩阵（同上）、
 * 人力缺口比对（{@code StaffPlanRuleServiceImpl}）。三处都只按这几个维度求和，
 * 所以聚合留在 SQL、折叠留在 service。
 *
 * <p>{@code orgName} 用 {@code MAX(org_name)} 取快照：0 人上班的单元不会出现在结果行里，
 * 单元名称缺口由 service 侧的标准行补，不要在这里 LEFT JOIN 出去。
 *
 * <p>{@code scheduleDate} 直接给 {@link LocalDate}：本列就是 DATE 类型，
 * 早前为了绕开裸 Map 取值才 {@code DATE_FORMAT} 成字符串，换 VO 后由 MyBatis 直接映射。
 */
@Data
public class StaffWorkingGroupVO implements Serializable {

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 排班单元类型（1-病区 2-科室 0-全院，见 OrgUnitTypeEnum）
     */
    private Integer orgType;

    /**
     * 排班单元ID（全院固定为 0）
     */
    private Long orgId;

    /**
     * 排班单元名称快照
     */
    private String orgName;

    /**
     * 班次ID；0 表示全班次共用（人力缺口比对里按跨班次合计处理）
     */
    private Long shiftId;

    /**
     * 岗位类型（见 StaffTypeEnum）
     */
    private Integer staffType;

    /**
     * 该组合下的在岗人次（COUNT(*)，同一人同日两班算两份）
     */
    private Long cnt;
}
