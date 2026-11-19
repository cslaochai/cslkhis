package com.his.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 值班类型（值班线）—— 对标厂商「自定义添加值班类型」的建模落点。
 *
 * <p>为什么不是枚举：医疗总值班 / 护理总值班 / 行政总值班 / 院领导带班 只是本院当前的四种，
 * 信息中心总值、后勤总值、分院区总值随时会加。写死枚举等于每加一条线就发一次版，
 * 而这条线的三个属性（归口科室、资质要求、是否有带班领导层）都是院办自己该维护的配置。
 *
 * <p>归口科室 {@code manageDeptId} 是权限的锚：谁排这条线，由「当前登录岗位的科室」与它比对，
 * 而不是另发一套按线划分的权限码（同一套 org:duty:edit，线由数据决定）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_duty_type")
public class BizDutyType extends BaseEntity {

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 类型编码（唯一，供前端与点位编码派生复用）
     */
    private String typeCode;

    /**
     * 类型名称（医疗总值班 / 护理总值班 / 行政总值班 / 院领导带班）
     */
    private String typeName;

    /**
     * 归口科室ID：这条值班线由哪个部门排班（权限锚点）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long manageDeptId;

    /**
     * 归口科室名称（快照）
     */
    private String manageDeptName;

    /**
     * 应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-不限）
     */
    private Integer requiredStaffType;

    /**
     * 允许的职务码值（字典 hospital_position，逗号分隔，空-不限）
     *
     * <p>制度要求：医疗总值班由临床、医技科室正/副主任担任，护理总值班由护士长担任。
     * 存码值不存文案：文案会随字典改，码值不会。
     */
    private String requiredPositions;

    /**
     * 是否需要院领导带班层（1-是 0-否）
     */
    private Integer leaderLayer;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
