package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 值守点位定义 —— 把「位」从「人」里剥出来。
 *
 * <p><b>为什么要有它</b>：值班那条线原先的唯一键是「日期 × 班次 × 主副」，也就是
 * 一个责任位一天只能有一位，但「位」本身从来没有定义过：全院总值班有几个位、各归哪个范围、
 * 该由什么岗位承接、值班电话是多少，全靠排班时人记。结果是同一天的白班只能存在一条，
 * 想再加一个「急诊总值班白班」就会撞键，而撞出来的报错看着像 bug。
 * 点位先定义存在，排班只是把人写进位里。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_duty_post")
public class BizDutyPost extends BaseEntity {

    /** 点位编码 */
    private String postCode;

    /** 点位名称 */
    private String postName;

    /** 责任范围（1-全院行政 2-急诊 3-感染 4-总务 5-信息） */
    private Integer dutyScope;

    /** 排班单元类型（1-科室 2-病区 3-全院） */
    private Integer orgType;

    /** 排班单元ID（全院级为 0） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /** 班内角色（1-主班 2-副班） */
    private Integer roleType;

    /**
     * 值班层级（0-不适用 1-一线 2-二线 3-三线，sql/202）
     *
     * <p>层挂在<b>点位</b>上而不是排班时手填：位先于人存在，「这个位是一线还是三线」是位的属性，
     * 换谁顶班都不该变。排班时再填，同一个位会随排班人不同出现不同层级，一线叫不动人的时候
     * 根本查不出该升给谁。
     */
    private Integer dutyLevel;

    /**
     * 响应形态（1-坐班 2-听班 3-留院值班，sql/202）
     *
     * <p>一线是留院值班，二线三线是听班 —— 同样是「今天有班」，一个在医院一个在家，
     * 催班和升级的处置完全不同。
     */
    private Integer attendMode;

    /** 标准班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /** 应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-不限） */
    private Integer requiredStaffType;

    /** 点位值班电话 */
    private String phone;

    /** 排序号 */
    private Integer sortNo;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}
