package com.his.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 护理排班模板规则（班次实用排班模式）：APN 三班倒 / 8-8 白夜两班 / 小组排班 等可复用模式。
 * 是新建模板时的「脚手架」参考，不是强约束——模板仍可自由增删明细行。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nurse_schedule_template_rule")
public class BizNurseScheduleTemplateRule extends BaseEntity {

    /** 逻辑删除标志（0 未删除 1 已删除；手动维护，删除走置位更新） */
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /** 模式名称（如「APN 三班倒」） */
    private String ruleName;

    /** 模式编码（APN / 8-8 / GROUP，业务约定，唯一语义） */
    private String ruleCode;

    /**
     * 班次模式明细（JSON：该模式由哪些班次组成、排班结构说明）。
     * 例如 {"shifts":["早班","中班","夜班"],"desc":"三班倒，每班 8 小时覆盖 24h"}，
     * 前端只读展示，服务端不解析。
     */
    private String patternDetail;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}
