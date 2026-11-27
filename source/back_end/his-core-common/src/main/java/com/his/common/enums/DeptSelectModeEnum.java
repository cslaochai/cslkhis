package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 科室树形下拉的选择模式：回答「这棵树上哪些节点允许被选中」。
 *
 * <p>与 {@link DeptNodeTypeEnum}（节点固有角色）正交：节点能不能选，先看固有角色
 * （片区/行政后勤/根节点恒不可选），再按本模式收窄到叶子或放开业务父节点。
 */
@Getter
public enum DeptSelectModeEnum {

    /** 仅末级业务科室：单据必须落到有人值守的具体科室，父节点仅作分组展示 */
    LEAF("LEAF", "仅末级业务科室"),
    /** 父子均可：业务父节点可选，选中后按子树聚合查询；片区/行政后勤仍不可选 */
    TREE("TREE", "父子均可（父节点按子树聚合）"),
    /** 组织归属维护：除根节点外全部可选（含片区、行政后勤），用于人员/单元挂载 */
    ORG("ORG", "组织归属维护"),
    ;

    private final String code;
    private final String label;

    DeptSelectModeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeptSelectModeEnum fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return TREE;
        }
        for (DeptSelectModeEnum item : values()) {
            if (item.code.equalsIgnoreCase(code.trim())) {
                return item;
            }
        }
        return TREE;
    }

    public static String getLabel(String code) {
        DeptSelectModeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }
}
