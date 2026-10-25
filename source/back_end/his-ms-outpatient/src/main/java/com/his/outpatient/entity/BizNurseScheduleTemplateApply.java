package com.his.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 护理排班模板适用记录（套用历史，审计用，只写不删）：谁在何时把哪个模板套到了哪个单元哪一周，
 * 生成/跳过多少格。对应截图「适用记录」页签。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nurse_schedule_template_apply")
public class BizNurseScheduleTemplateApply extends BaseEntity {

    /** 备注 */
    private String remark;

    /** 套用模板ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /** 套用模板名称（快照） */
    private String templateName;

    /** 排班单元类型（1-病区 2-门诊科室） */
    private Integer unitType;

    /** 排班单元ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 排班单元名称（快照） */
    private String wardName;

    /** 套用目标周起始（周一） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyWeekStart;

    /** 套用目标周结束（周日） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyWeekEnd;

    /** 操作人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long appliedById;

    /** 操作人姓名 */
    private String appliedByName;

    /** 实际生成排班格数 */
    private Integer generatedCount;

    /** 跳过格数（已排不覆盖 / 日期已过 / 单双周不匹配 / 不在生效期 / 班次失效等） */
    private Integer skippedCount;

    /** 状态（0-成功 1-部分跳过） */
    private Integer status;
}
