package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 排班变更类型枚举（sql/200，字典 his_schedule_change_type）
 */
@Getter
public enum ScheduleChangeTypeEnum {

    /**
     * 换班：两人互换班次
     */
    SWAP(1, "换班"),
    /**
     * 代班：临时换人顶班，原班归属不变
     */
    SUBSTITUTE(2, "代班"),
    /**
     * 停班：整班取消（抽调查封、科室停摆）
     */
    SUSPEND(3, "停班"),
    /**
     * 加号：在已排班上新增号源
     */
    ADD_SOURCE(4, "加号"),
    /**
     * 减号：收回未发出的号源
     */
    REDUCE_SOURCE(5, "减号"),
    /**
     * 出诊变更：改诊室/时段/班别等出诊属性
     */
    CLINIC_CHANGE(6, "出诊变更"),
    /**
     * 提交审核：草稿 → 待审核
     */
    SUBMIT(7, "提交审核"),
    /**
     * 审核发布：待审核 → 已发布
     */
    PUBLISH(8, "审核发布"),
    /**
     * 驳回：待审核 → 草稿（附原因）
     */
    REJECT(9, "驳回"),
    /**
     * 整周提交：本单元整周草稿 → 待审核
     */
    SUBMIT_WEEK(10, "整周提交"),
    /**
     * 整周审核发布：本单元整周待审核 → 已发布
     */
    PUBLISH_WEEK(11, "整周审核发布"),
    /**
     * 撤回：任意态 → 草稿
     */
    RECALL(12, "撤回"),
    /**
     * 进入调班：已发布/已变更 → 调班中
     */
    ADJUST(13, "进入调班");

    private final int code;
    private final String label;

    ScheduleChangeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static ScheduleChangeTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ScheduleChangeTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ScheduleChangeTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ScheduleChangeTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ScheduleChangeTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
