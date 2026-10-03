package com.his.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班次下拉出参，字段口径同 ShiftVO
 * （ShiftVO 被 /shift/listPage 共用，下拉侧单独收口）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftSelectListVO extends ShiftVO {
}
