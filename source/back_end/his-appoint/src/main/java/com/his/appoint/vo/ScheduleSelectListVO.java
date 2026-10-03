package com.his.appoint.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 排班下拉出参，字段口径同 ScheduleDetailVO
 * （ScheduleDetailVO 被 /schedule/list、/schedule/today 共用，下拉侧单独收口）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSelectListVO extends ScheduleDetailVO {
}
