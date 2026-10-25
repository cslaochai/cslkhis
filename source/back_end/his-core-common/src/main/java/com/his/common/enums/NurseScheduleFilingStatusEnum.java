package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 护理排班周备案态枚举（0-待备案 1-已备案 2-已退回），挂在「护理单元 × 排班周」上。
 *
 * <p>与行发布态（草稿/待审核/已发布/调班中/已变更）是<b>正交维度</b>：科护士长审核发布即生效计在岗，
 * 护理部备案只是留档抽查，不阻塞生效；备案退回才整周把生效行打回草稿重排。
 */
@Getter
public enum NurseScheduleFilingStatusEnum {

    /** 待备案：本周该单元已由片区审核发布，等护理部备案 */
    PENDING(0, "待备案"),
    /** 已备案：护理部已确认留档 */
    FILED(1, "已备案"),
    /** 已退回：护理部抽查退回，本周生效行已打回草稿重排 */
    REJECTED(2, "已退回");

    /**
     * 码值用 {@code Integer}：调用侧要写 {@code FILED.getCode().equals(nullable)}，null 安全。
     */
    private final Integer code;
    private final String label;

    NurseScheduleFilingStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NurseScheduleFilingStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NurseScheduleFilingStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /** 展示口径：合法→label；null/脏值→空串 */
    public static String getLabel(Integer code) {
        NurseScheduleFilingStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    /** 调用方自带缺省文案 */
    public static String getLabel(Integer code, String defaultValue) {
        NurseScheduleFilingStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    /** 异常/审计口径：null/脏值→未知(n)，保留原始码值便于排查 */
    public static String getLabelOrUnknown(Integer code) {
        NurseScheduleFilingStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
