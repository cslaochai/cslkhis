package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 护理排班审批与变更类型枚举（护理排班留痕的动作码，字典 his_nurse_schedule_change_type）。
 *
 * <p>与门诊号源留痕 {@link ClinicSourceChangeTypeEnum}、岗位换班留痕分开：护理周排班的审计粒度是
 * 「哪个护士哪天的格子被排/改/删，以及整行/整周的提交、审核发布、撤回、调班」，码值语义独立。
 */
@Getter
public enum NurseScheduleChangeTypeEnum {

    /** 新增排班：单格新增或批量铺班展开的一条 */
    CREATE(1, "新增排班"),
    /** 修改/调班：改格、已发布后的调班（记新旧班次/状态快照） */
    UPDATE(2, "修改/调班"),
    /** 删除排班：清空格子 */
    DELETE(3, "删除排班"),
    /** 提交审核：草稿 → 待审核（单行） */
    SUBMIT(4, "提交审核"),
    /** 审核发布：待审核 → 已发布（单行） */
    PUBLISH(5, "审核发布"),
    /** 撤回：任意态 → 草稿（单行） */
    RECALL(6, "撤回"),
    /** 进入调班：已发布/已变更 → 调班中（单行锁定） */
    ADJUST(7, "进入调班"),
    /** 整周提交：本周草稿一次性 → 待审核 */
    SUBMIT_WEEK(8, "整周提交"),
    /** 整周发布：本周待审核一次性 → 已发布 */
    PUBLISH_WEEK(9, "整周发布"),
    /** 复制上周：把来源周格子铺到目标周空缺 */
    COPY_WEEK(10, "复制上周"),
    /** 套用模板：把排班模板明细铺到目标周空缺，来源=TEMPLATE */
    APPLY_TEMPLATE(11, "套用模板"),
    /** 备案：护理部对片区已发布的护理单元周次确认留档（不阻塞在岗生效） */
    FILING(12, "备案"),
    /** 备案退回：护理部退回已发布周次，该周生效行整批打回草稿重排 */
    FILING_REJECT(13, "备案退回");

    private final int code;
    private final String label;

    NurseScheduleChangeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NurseScheduleChangeTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NurseScheduleChangeTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        NurseScheduleChangeTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NurseScheduleChangeTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
