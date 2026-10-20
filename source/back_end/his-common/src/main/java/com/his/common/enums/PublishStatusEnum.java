package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 排班发布态 —— 审核发布流程的「第三状态维度」，独立于：
 * <ul>
 *   <li>出诊源 {@code biz_clinic_source.status}（号源可用性：停诊/正常/已满/已过期）</li>
 *   <li>员工排班 {@code biz_schedule.duty_status}（出勤态：上班/休息/请假/培训/停班）</li>
 *   <li>值守 {@code biz_duty_roster.status}（有效/停用）</li>
 * </ul>
 *
 * <p>流转：草稿(0) → 待审核(1) → 已发布(2) → 调班中(3) → 已变更(4)；已发布/已变更可撤回回草稿。
 * 业务约定（2026-10-19 老王定）：排班人保存为草稿 → 提交进待审核 → 审核人审核通过即发布（已发布）；
 * 已发布后改单进调班中（锁定） → 改完保存直接已变更（不二次审核）。
 *
 * <p>生效判定：{@link #isEffective(Integer)} 为 true（即已发布/已变更）才算正式生效，
 * 才对外出号、计入需求层/出勤底座、全院排班与总值班展示。
 */
@Getter
public enum PublishStatusEnum {

    /**
     * 草稿：排班人保存的初态，不生效
     */
    DRAFT(0, "草稿"),
    /**
     * 待审核：已提交，等审核人审核
     */
    PENDING(1, "待审核"),
    /**
     * 已发布：审核通过，正式生效（出号/计在岗/展示）
     */
    PUBLISHED(2, "已发布"),
    /**
     * 调班中：已发布后进入改单，期间锁定（号源冻结、不让约）
     */
    ADJUSTING(3, "调班中"),
    /**
     * 已变更：调班完成，改完后直接生效（不二次审核）
     */
    CHANGED(4, "已变更");

    /**
     * 码值用 {@code Integer} 而非 {@code int}：全仓枚举的 {@code getCode()} 一律返回包装类型，
     * 调用侧才能写 {@code XxxEnum.STATUS.getCode().equals(someNullableInteger)}（null 安全）。
     * 返回基本类型时 {@code getCode().equals(...)} 会编译不过（对 int 解引用）。
     */
    private final Integer code;
    private final String label;

    PublishStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PublishStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PublishStatusEnum item : values()) {
            // 值比较：code 是 Integer，== 会变成引用比较（-128~127 才碰巧对）
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 是否处于「正式生效」状态（可出号/计在岗/对外展示）。null 按不生效处理。
     */
    public static boolean isEffective(Integer code) {
        return code != null && (code.equals(PUBLISHED.code) || code.equals(CHANGED.code));
    }

    /**
     * 展示口径：脏码值出空串
     */
    public static String getLabel(Integer code) {
        PublishStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PublishStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
