package com.his.common.enums;

import lombok.Getter;

/**
 * 门诊排班变更类型枚举（sql/261，字典 his_clinic_source_change_type）。
 *
 * <p>与岗位排班留痕 {@link ScheduleChangeTypeEnum}（换班/代班/停班维度）分开：
 * 门诊号源的变更审计是按「哪个班次的号源/状态被改成了什么」落账，
 * 码值语义完全不同，共用一套枚举只会让两种留痕互相读不懂。
 */
@Getter
public enum ClinicSourceChangeTypeEnum {

    /**
     * 新增排班：单条新增或批量展开的一条
     */
    CREATE(1, "新增排班"),
    /**
     * 修改排班：改出诊属性（诊室/时段/号源/费用等）
     */
    UPDATE(2, "修改排班"),
    /**
     * 删除排班：整班删除
     */
    DELETE(3, "删除排班"),
    /**
     * 停诊：整班挂起，号段随之停用，在挂患者走批量退号
     */
    STOP(4, "停诊"),
    /**
     * 启用：停诊班次恢复放号
     */
    ENABLE(5, "启用"),
    /**
     * 加号：给已排班次追加号源
     */
    ADD_SOURCE(6, "加号"),
    /**
     * 批量退号：停诊引发的在挂患者集中退号
     */
    BATCH_CANCEL(7, "批量退号"),
    /**
     * 段级编辑：逐半小时档调整号源/预约池/停用状态
     */
    SLOT_EDIT(8, "段级编辑"),
    /**
     * 复刻：把源区间排班整体后移复制
     */
    REPLICATE(9, "复刻");

    private final int code;
    private final String label;

    ClinicSourceChangeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ClinicSourceChangeTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ClinicSourceChangeTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    public static String getText(Integer code) {
        ClinicSourceChangeTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }
}
